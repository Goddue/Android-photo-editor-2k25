package com.example.laba

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.hypot

// --- Data Classes and Enums ---
// These should be accessible by both ImageEditorView and ekran_redact.kt

sealed class ImageOverlay(
    open var bounds: RectF, // Position and size in image coordinates
    open var rotation: Float = 0f // Rotation in degrees
)

data class TextOverlay(
    override var bounds: RectF,
    override var rotation: Float = 0f,
    var text: String,
    var textSize: Float, // Text size in image coordinates/scale
    var textColor: Int = Color.WHITE
) : ImageOverlay(bounds, rotation)

// ShapeOverlay, StickerOverlay, ShapeType were removed in previous version, keeping it simplified

enum class EditingMode { NONE, CROP, OVERLAY, DRAWING }


// --- ImageEditorView Class ---
class ImageEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- Data and State managed by View ---
    private var baseBitmap: Bitmap? = null // Main bitmap after destructive changes (like crop)
    private var colorMatrix: ColorMatrix? = null // Combined filter matrix from Activity
    private var overlays: MutableList<ImageOverlay> = mutableListOf() // List of overlay objects (only TextOverlay)
    private var drawingPaths: MutableList<Path> = mutableListOf() // List of paths for free drawing, managed here
    private var currentEditingMode: EditingMode = EditingMode.NONE // Current editing mode, managed here

    // --- Brush Drawing: State ---
    private var currentBrushColor: Int = Color.BLACK // Default brush color
    private var currentBrushStrokeWidth: Float = 10f // Default brush size


    // --- Internal Drawing Variables ---
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val overlayPaint = Paint() // Paint for overlay border/handles
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { // Paint for text fill
        textAlign = Paint.Align.LEFT // Default text alignment
    }
    private val drawingPaint = Paint().apply { // Paint for sketch/drawing brush
        isAntiAlias = true
        isDither = true
        color = currentBrushColor // Use state variable
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = currentBrushStrokeWidth // Use state variable
    }
    private val cropPaint = Paint().apply { // Paint for crop border and handles (visualization only)
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val dimmedPaint = Paint().apply { // Paint to dim area outside crop
        color = Color.BLACK
        alpha = 150 // Semi-transparent black
    }

    // --- Internal Interactive State ---
    private val viewToImageMatrix = Matrix() // Matrix to transform View coords to Image coords
    private val imageToViewMatrix = Matrix() // Matrix to transform Image coords to View coords

    // Overlay interaction state
    private var selectedOverlay: ImageOverlay? = null // Only TextOverlay
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private val touchPoints = SparseArray<PointF>() // For tracking multiple touches
    private var initialPinchDistance = 0f // For scaling
    private var initialPinchAngle = 0f // For rotation
    private var initialOverlayBounds: RectF? = null // For saving initial bounds during scaling
    private var initialOverlayRotation: Float = 0f // For saving initial rotation
    private var initialTextSize: Float = 0f // For saving initial text size

    // Crop interaction state (variables kept for visualization)
    private val cropRectView = RectF() // Current crop rectangle in View coordinates
    private val cropRectImage = Rect() // Current crop rectangle in Image coordinates


    // Drawing interaction state
    private var currentDrawingPath: Path? = null
    private val currentImagePoint = PointF() // Reusable point for transformation
    private val lastImagePoint = PointF() // Reusable point for transformation


    // --- Setters called from Activity ---

    fun setBitmap(bitmap: Bitmap?) {
        this.baseBitmap = bitmap
        updateMatrices() // Update matrices when bitmap changes
        // Reset all editing states when setting a new bitmap
        resetInteractionState()
        // If bitmap is set, initialize default crop rect (now only for visualization)
        if (bitmap != null) {
            cropRectImage.set(0, 0, bitmap.width, bitmap.height) // Default to full image crop
            mapImageRectToViewRect(cropRectImage, cropRectView) // Map to view coords
        }
        invalidate() // Redraw View
    }

    fun setColorMatrix(matrix: ColorMatrix?) {
        this.colorMatrix = matrix
        bitmapPaint.colorFilter = if (matrix != null) ColorMatrixColorFilter(matrix) else null
        invalidate() // Redraw View
    }

    fun addOverlay(overlay: ImageOverlay) {
        // Only accept TextOverlay
        if (overlay is TextOverlay) {
            this.overlays.add(overlay)
            invalidate() // Redraw View
        }
    }

    fun setOverlays(overlays: List<ImageOverlay>) {
        // Only accept TextOverlay in the list
        this.overlays = overlays.filterIsInstance<TextOverlay>().toMutableList()
        invalidate()
    }

    fun setDrawingPaths(paths: List<Path>) {
        this.drawingPaths = paths.toMutableList()
        invalidate()
    }

    fun setEditingMode(mode: EditingMode) {
        this.currentEditingMode = mode
        // Reset state based on the new mode
        resetInteractionState() // Also resets cropRectView
        invalidate() // Redraw to show/hide interactive elements
    }

    // --- Drawing Brush Control ---
    fun setBrushColor(color: Int) {
        currentBrushColor = color
        drawingPaint.color = currentBrushColor // Update Paint object
        // No need for invalidate() here, redraw happens during drawing or mode change
    }

    fun setBrushStrokeWidth(width: Float) {
        currentBrushStrokeWidth = width
        drawingPaint.strokeWidth = currentBrushStrokeWidth // Update Paint object
        // No need for invalidate() here
    }


    // --- Methods to get final state for applying/saving (called from Activity) ---

    // Get the current crop rectangle in image coordinates
    // Note: This function's usefulness is limited if percentage crop is the primary method.
    // It currently maps the View's interactive crop rect (if used) or the full image bounds.
    fun getCropRectImage(): Rect {
        mapViewRectToImageRect(cropRectView, cropRectImage) // Update based on view bounds
        return cropRectImage // Return the last mapped view rect (or full image if reset)
    }

    // Get the final state of overlays (only TextOverlay)
    fun getOverlays(): List<ImageOverlay> {
        return overlays.toList() // Return a copy to prevent external modification
    }

    // Get the final drawn paths in image coordinates
    fun getDrawingPathsImage(): List<Path> {
        return drawingPaths.toList() // Return a copy
    }

    // Get the drawing brush properties
    fun getDrawingPaintProperties(): Paint {
        return Paint(drawingPaint) // Return a copy of the current Paint state
    }


    // --- Coordinate Transformations ---
    private fun updateMatrices() {
        baseBitmap?.let { bitmap ->
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) {
                viewToImageMatrix.reset()
                imageToViewMatrix.reset()
                return
            }

            val scale: Float
            val dx: Float
            val dy: Float

            val viewAspectRatio = viewWidth / viewHeight
            val imageAspectRatio = imageWidth / imageHeight

            // Logic for FIT_CENTER
            if (imageAspectRatio > viewAspectRatio) { // Image is wider than View
                scale = viewWidth / imageWidth
                dx = 0f
                dy = (viewHeight - imageHeight * scale) / 2f
            } else { // Image is taller or same aspect ratio as View
                scale = viewHeight / imageHeight
                dx = (viewWidth - imageWidth * scale) / 2f
                dy = 0f
            }

            imageToViewMatrix.reset()
            imageToViewMatrix.postScale(scale, scale)
            imageToViewMatrix.postTranslate(dx, dy)

            // Calculate the inverse matrix
            imageToViewMatrix.invert(viewToImageMatrix)
        }
    }

    // Transform point from View coordinates to Image coordinates
    private fun mapViewPointToImagePoint(vx: Float, vy: Float, imagePoint: PointF) {
        val pts = floatArrayOf(vx, vy)
        viewToImageMatrix.mapPoints(pts)
        imagePoint.set(pts[0], pts[1])
    }

    // Transform rectangle from Image coordinates to View coordinates
    private fun mapImageRectToViewRect(imageRect: Rect, viewRect: RectF) {
        val pts = floatArrayOf(
            imageRect.left.toFloat(), imageRect.top.toFloat(),
            imageRect.right.toFloat(), imageRect.bottom.toFloat()
        )
        imageToViewMatrix.mapPoints(pts)
        viewRect.set(pts[0], pts[1], pts[2], pts[3])
    }

    // Transform rectangle from View coordinates to Image coordinates
    private fun mapViewRectToImageRect(viewRect: RectF, imageRect: Rect) {
        val pts = floatArrayOf(
            viewRect.left, viewRect.top,
            viewRect.right, viewRect.bottom
        )
        viewToImageMatrix.mapPoints(pts)
        // Use roundToInt or coerceIn for safer conversion if needed
        imageRect.set(pts[0].toInt(), pts[1].toInt(), pts[2].toInt(), pts[3].toInt())
    }


    // --- Drawing Logic ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw the base image (with color filter if applied)
        baseBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, imageToViewMatrix, bitmapPaint)
        }

        // 2. Draw interactive elements based on mode
        when (currentEditingMode) {
            // CROP mode is visualization only here based on activity's percentage crop
            EditingMode.CROP -> {
                baseBitmap?.let { bitmap ->
                    // Draw dimmed area outside the cropRectView
                    val path = Path()
                    val fullImageRectView = RectF()
                    mapImageRectToViewRect(Rect(0,0, bitmap.width, bitmap.height), fullImageRectView)

                    path.addRect(fullImageRectView, Path.Direction.CW) // Outer rectangle (full image area in view coords)
                    path.addRect(cropRectView, Path.Direction.CCW) // Inner rectangle (current view crop rect)

                    canvas.drawPath(path, dimmedPaint)
                    canvas.drawRect(cropRectView, cropPaint) // Draw crop rectangle border
                    // TODO: Draw handles if needed for visualization
                }
            }
            EditingMode.OVERLAY -> {
                // Draw all overlays interactively (only TextOverlay)
                overlays.forEach { overlay ->
                    if (overlay is TextOverlay) { // Check for TextOverlay
                        drawOverlayInteractive(canvas, overlay)
                    }
                }
                // Draw selection UI for the selected overlay (only TextOverlay)
                selectedOverlay?.let {
                    if (it is TextOverlay) { // Check for TextOverlay
                        drawOverlaySelectionUI(canvas, it)
                    }
                }
            }
            EditingMode.DRAWING -> {
                // Draw drawing paths in image coordinates, apply matrix during draw
                drawingPaths.forEach { path ->
                    canvas.save()
                    canvas.concat(imageToViewMatrix) // Apply the matrix before drawing the path
                    canvas.drawPath(path, drawingPaint) // Draw path in image coordinates
                    canvas.restore()
                }
                // Draw the current active drawing path
                currentDrawingPath?.let { path ->
                    canvas.save()
                    canvas.concat(imageToViewMatrix) // Apply the matrix
                    canvas.drawPath(path, drawingPaint) // Draw path in image coordinates
                    canvas.restore()
                }
            }
            EditingMode.NONE -> {
                // No interactive drawing in NONE mode
            }
        }
    }

    // Helper function to draw a single overlay interactively (only TextOverlay)
    private fun drawOverlayInteractive(canvas: Canvas, overlay: ImageOverlay) {
        canvas.save()

        val overlayViewBounds = RectF()
        mapImageRectToViewRect(overlay.bounds.toIntRect(), overlayViewBounds)

        // Apply rotation around the center of the overlay bounds in View coordinates
        canvas.rotate(overlay.rotation, overlayViewBounds.centerX(), overlayViewBounds.centerY())

        // Draw only TextOverlay
        if (overlay is TextOverlay) {
            textPaint.color = overlay.textColor
            textPaint.textSize = overlay.textSize * (imageToViewMatrix.mapRadius(1f)) // Scale text size based on matrix scale

            val fm = textPaint.fontMetrics
            // Adjust baseline for text centering within bounds in View coordinates
            val textBaselineY = overlayViewBounds.top - fm.ascent + (overlayViewBounds.height() - (fm.descent - fm.ascent)) / 2f

            canvas.drawText(overlay.text, overlayViewBounds.left, textBaselineY, textPaint)
        }

        canvas.restore()
    }

    // Helper function to draw selection UI/handles for an overlay (only TextOverlay)
    private fun drawOverlaySelectionUI(canvas: Canvas, overlay: ImageOverlay) {
        canvas.save()
        val overlayViewBounds = RectF()
        mapImageRectToViewRect(overlay.bounds.toIntRect(), overlayViewBounds)
        // Apply the same rotation as the overlay, around its center in View
        canvas.rotate(overlay.rotation, overlayViewBounds.centerX(), overlayViewBounds.centerY())

        // Draw selection UI only for the selected overlay (now only TextOverlay)
        if (overlay is TextOverlay) {
            overlayPaint.color = Color.BLUE
            overlayPaint.style = Paint.Style.STROKE
            overlayPaint.strokeWidth = 4f / (imageToViewMatrix.mapRadius(1f)) // Scale stroke width
            canvas.drawRect(overlayViewBounds, overlayPaint)

            // TODO: Calculate and draw handles (e.g., at corners)
            val handleSizeView = 30f / (imageToViewMatrix.mapRadius(1f)) // Scale handle size
            overlayPaint.color = Color.YELLOW
            overlayPaint.style = Paint.Style.FILL
            canvas.drawCircle(overlayViewBounds.left, overlayViewBounds.top, handleSizeView / 2, overlayPaint) // Top left
            canvas.drawCircle(overlayViewBounds.right, overlayViewBounds.top, handleSizeView / 2, overlayPaint) // Top right
            canvas.drawCircle(overlayViewBounds.left, overlayViewBounds.bottom, handleSizeView / 2, overlayPaint) // Bottom left
            canvas.drawCircle(overlayViewBounds.right, overlayViewBounds.bottom, handleSizeView / 2, overlayPaint) // Bottom right
        }

        canvas.restore()
    }

    // Helper to convert RectF to Rect (with loss of precision)
    private fun RectF.toIntRect(): Rect {
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }


    // --- Touch Event Handling ---

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (currentEditingMode) {
            EditingMode.NONE -> {
                return false // Do not consume touch events in NONE mode
            }
            // CROP mode is not interactive via touch in this View based on previous code comments
            EditingMode.CROP -> {
                return false // Do not process touches here for percentage crop
            }
            EditingMode.OVERLAY -> {
                // Handle touch events for overlays (only TextOverlay)
                return handleOverlayTouchEvent(event)
            }
            EditingMode.DRAWING -> {
                // Handle touch events for drawing mode
                return handleDrawingTouchEvent(event)
            }
        }
        // Should not reach here, but required by function signature
        return super.onTouchEvent(event)
    }

    // --- Overlay Touch Event Handling ---
    private fun handleOverlayTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                touchPoints.clear()
                for (i in 0 until event.pointerCount) {
                    touchPoints.put(event.getPointerId(i), PointF(event.getX(i), event.getY(i)))
                }

                // Find if a TextOverlay is touched
                selectedOverlay = findInteractiveOverlayAtPoint(lastTouchX, lastTouchY)

                // If an overlay is found, save its initial parameters
                if (selectedOverlay != null) {
                    initialOverlayBounds = RectF(selectedOverlay!!.bounds)
                    initialOverlayRotation = selectedOverlay!!.rotation
                    if (selectedOverlay is TextOverlay) {
                        initialTextSize = (selectedOverlay as TextOverlay).textSize
                    }
                    // TODO: Detect if a resize or rotate handle is hit if selectedOverlay is found
                }

                return selectedOverlay != null // Consume the event only if an overlay is selected
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                touchPoints.put(pointerId, PointF(event.getX(pointerIndex), event.getY(pointerIndex)))

                if (event.pointerCount >= 2 && selectedOverlay != null) { // Need at least 2 fingers
                    // Initialize pinch/rotate on second finger down
                    initialPinchDistance = calculateDistance(touchPoints.get(event.getPointerId(0)), touchPoints.get(event.getPointerId(1)))
                    initialPinchAngle = calculateAngle(touchPoints.get(event.getPointerId(0)), touchPoints.get(event.getPointerId(1)))
                    initialOverlayBounds = RectF(selectedOverlay!!.bounds) // Resave initial bounds
                    initialOverlayRotation = selectedOverlay!!.rotation // Resave initial rotation
                    if (selectedOverlay is TextOverlay) {
                        initialTextSize = (selectedOverlay as TextOverlay).textSize // Resave initial text size
                    }
                }
                return selectedOverlay != null // Consume only if there's a selected overlay
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    touchPoints.put(pointerId, PointF(event.getX(i), event.getY(i)))
                }

                if (selectedOverlay != null && initialOverlayBounds != null) { // Ensure overlay is selected and initial parameters are saved

                    // --- Drag Logic (if only one finger active) ---
                    if (event.pointerCount == 1) {
                        val dxView = event.x - lastTouchX
                        val dyView = event.y - lastTouchY
                        lastTouchX = event.x
                        lastTouchY = event.y

                        val imageDxDy = floatArrayOf(dxView, dyView)
                        viewToImageMatrix.mapVectors(imageDxDy) // Transform translation vector

                        selectedOverlay?.bounds?.offset(imageDxDy[0], imageDxDy[1])

                        // TODO: Add boundary checks

                        invalidate()

                        // Reset multitouch state during single-finger drag
                        initialPinchDistance = 0f
                        initialPinchAngle = 0f
                        initialOverlayBounds = RectF(selectedOverlay!!.bounds) // Update initial bounds for smoothness
                        initialOverlayRotation = selectedOverlay!!.rotation // Update initial rotation
                        if (selectedOverlay is TextOverlay) {
                            initialTextSize = (selectedOverlay as TextOverlay).textSize // Update initial text size
                        }

                    }
                    // --- Scale/Rotate Logic (if two or more fingers active) ---
                    else if (event.pointerCount >= 2 && initialPinchDistance > 0 && initialOverlayBounds != null) {
                        val p1 = touchPoints.get(event.getPointerId(0))
                        val p2 = touchPoints.get(event.getPointerId(1))

                        // Scaling
                        val currentPinchDistance = calculateDistance(p1, p2)
                        if (currentPinchDistance > 0 && initialPinchDistance > 0) {
                            val scaleFactor = currentPinchDistance / initialPinchDistance

                            val initialCenterX = initialOverlayBounds!!.centerX()
                            val initialCenterY = initialOverlayBounds!!.centerY()

                            val scaleMatrix = Matrix()
                            scaleMatrix.postScale(scaleFactor, scaleFactor, initialCenterX, initialCenterY)

                            selectedOverlay!!.bounds.set(initialOverlayBounds!!) // Start from initial bounds
                            scaleMatrix.mapRect(selectedOverlay!!.bounds)

                            // --- Scale Text Size ---
                            if (selectedOverlay is TextOverlay) {
                                if (initialOverlayBounds!!.height() > 0) {
                                    // Scale text size proportionally to bounds height change
                                    val heightScaleFactor = selectedOverlay!!.bounds.height() / initialOverlayBounds!!.height()
                                    (selectedOverlay as TextOverlay).textSize = initialTextSize * heightScaleFactor
                                }
                            }
                        }

                        // Rotation
                        if (initialPinchAngle != 0f) {
                            val currentPinchAngle = calculateAngle(p1, p2)
                            val angleDelta = currentPinchAngle - initialPinchAngle

                            selectedOverlay!!.rotation = (initialOverlayRotation + angleDelta) % 360
                            if (selectedOverlay!!.rotation < 0) selectedOverlay!!.rotation += 360
                        }
                        invalidate() // Redraw after updating bounds and rotation
                    }
                    // TODO: Add logic for handle dragging (resize/rotate)
                }
                return selectedOverlay != null || event.pointerCount > 1 // Consume if an overlay is selected or using multitouch
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)
                touchPoints.remove(pointerId)

                if (event.pointerCount == 1) {
                    // If only one finger remains after up, reset multitouch state
                    initialPinchDistance = 0f
                    initialPinchAngle = 0f
                    initialOverlayBounds = selectedOverlay?.bounds?.let { RectF(it) } // Save current bounds as new initial
                    initialOverlayRotation = selectedOverlay?.rotation ?: 0f // Save current rotation
                    if (selectedOverlay is TextOverlay) {
                        initialTextSize = (selectedOverlay as TextOverlay).textSize // Save current text size
                    }

                    if (touchPoints.size() > 0) {
                        lastTouchX = touchPoints.valueAt(0).x
                        lastTouchY = touchPoints.valueAt(0).y
                    }
                } else if (event.pointerCount == 0) {
                    // Last finger up, end of gesture
                    initialPinchDistance = 0f
                    initialPinchAngle = 0f
                    initialOverlayBounds = null
                    initialOverlayRotation = 0f
                    initialTextSize = 0f
                    // selectedOverlay remains selected until touch down elsewhere
                }
                // Consume if an overlay was selected or it's the end of a multi-touch gesture
                return selectedOverlay != null || event.actionMasked != MotionEvent.ACTION_UP // Consume if not a simple UP
            }
        }
        return false // Should not reach here
    }

    // --- Drawing Touch Event Handling ---
    private fun handleDrawingTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount > 1) return false // Only single touch for drawing

                currentDrawingPath = Path()
                mapViewPointToImagePoint(event.x, event.y, currentImagePoint)
                currentDrawingPath?.moveTo(currentImagePoint.x, currentImagePoint.y)
                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1) return false

                mapViewPointToImagePoint(event.x, event.y, currentImagePoint)
                mapViewPointToImagePoint(lastTouchX, lastTouchY, lastImagePoint)

                // Use quadTo for smoother drawing
                currentDrawingPath?.quadTo(lastImagePoint.x, lastImagePoint.y, (currentImagePoint.x + lastImagePoint.x) / 2f, (currentImagePoint.y + lastImagePoint.y) / 2f)

                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount > 1) return false

                mapViewPointToImagePoint(event.x, event.y, currentImagePoint)
                currentDrawingPath?.lineTo(currentImagePoint.x, currentImagePoint.y)

                currentDrawingPath?.let { path ->
                    drawingPaths.add(path) // Add the finished path
                }
                currentDrawingPath = null // Clear current path
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                currentDrawingPath = null // Discard the current path
                invalidate()
                return true
            }
        }
        return false // Should not reach here
    }


    // --- Helper function to find an interactive overlay at a point ---
    // Searches only for TextOverlay for interactive editing
    private fun findInteractiveOverlayAtPoint(viewX: Float, viewY: Float): ImageOverlay? {
        val imagePoint = PointF()
        mapViewPointToImagePoint(viewX, viewY, imagePoint) // Transform touch point to image coordinates

        // Iterate from top (last drawn) to bottom
        for (i in overlays.indices.reversed()) {
            val overlay = overlays[i]

            if (overlay !is TextOverlay) {
                continue // Only interactive with TextOverlay
            }

            // Simple bounds check in image coordinates
            // This is a basic check and doesn't account for rotation or complex shapes.
            // For simplicity, we'll use image bounds transformed back to View for a click check.
            val overlayViewBounds = RectF()
            mapImageRectToViewRect(overlay.bounds.toIntRect(), overlayViewBounds)

            // Check hit in View coordinates (simpler collision for rotated rectangles is complex)
            // A more robust solution would use inverse rotation matrix on the touch point
            // and check against axis-aligned bounds. For simplicity, using View bounds check.
            if (overlayViewBounds.contains(viewX, viewY)) {
                // TODO: Add specific handle hit detection here if handles are interactive
                return overlay
            }

        }

        return null // No interactive overlay found at this point
    }


    // --- Helper functions for multitouch gestures ---
    private fun calculateDistance(p1: PointF, p2: PointF): Float {
        return hypot(p2.x - p1.x, p2.y - p1.y)
    }

    private fun calculateAngle(p1: PointF, p2: PointF): Float {
        return Math.toDegrees(atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())).toFloat()
    }


    // Required when overriding onTouchEvent
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMatrices() // Recalculate matrices when View size changes

        // Update cropRectView based on the new View size if CROP mode is active (for visualization)
        if (currentEditingMode == EditingMode.CROP && baseBitmap != null) {
            mapImageRectToViewRect(cropRectImage, cropRectView) // Update view crop rect based on image crop rect
        }
    }

    // Method to clear interactive state
    fun resetInteractionState() {
        selectedOverlay = null
        currentDrawingPath = null
        touchPoints.clear()
        initialPinchDistance = 0f
        initialPinchAngle = 0f
        initialOverlayBounds = null
        initialOverlayRotation = 0f
        initialTextSize = 0f
        // Reset cropRectView to represent current image bounds in View coordinates (for visualization)
        baseBitmap?.let { bitmap ->
            cropRectImage.set(0, 0, bitmap.width, bitmap.height) // Reset image crop rectangle to full image
            mapImageRectToViewRect(cropRectImage, cropRectView) // Transform to View coordinates
        } ?: cropRectView.setEmpty() // If no bitmap, set to empty

        invalidate()
    }
}