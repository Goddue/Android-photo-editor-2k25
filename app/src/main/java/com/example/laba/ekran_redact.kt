package com.example.laba

// --- Imports ---
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Time
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream
import okhttp3.* // Although imported, okhttp3 is not used in the provided code snippet
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

// Note: Data classes and enums (ImageOverlay, TextOverlay, EditingMode)
// are assumed to be defined in ImageEditorView.kt and accessible here.

// --- Main Activity Class ---
class ekran_redact : AppCompatActivity() {

    // --- View Variables ---
    private lateinit var imageEditorView: ImageEditorView
    private lateinit var btnNazad: ImageButton // Renamed for clarity
    private lateinit var btnSave: ImageButton // Renamed for clarity
    private lateinit var sliderContainer: LinearLayout
    private lateinit var slider: SeekBar
    private lateinit var editingControlsContainer: LinearLayout
    private lateinit var cropOptionsContainer: LinearLayout
    private lateinit var brushControlsContainer: LinearLayout
    private lateinit var brushSizeSlider: SeekBar
    private lateinit var btnColorBlack: Button
    private lateinit var btnColorRed: Button
    private lateinit var btnColorGreen: Button
    private lateinit var btnColorBlue: Button
    private lateinit var btnCropVertical: Button
    private lateinit var btnCropHorizontal: Button
    private lateinit var btnCropAllSides: Button
    private lateinit var toolsLinearLayout: LinearLayout // Reference to the main tools layout

    // --- State Variables ---
    private var originalBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null
    private val filterValues = mutableMapOf<String, Int>()
    private var currentFilter: String? = null
    private var currentEditingMode: EditingMode = EditingMode.NONE

    // Map filter tool LinearLayout IDs to filter names
    private val filterToolMap = mapOf(
        R.id.tool_brightness to "brightness",
        R.id.tool_contrast to "contrast",
        R.id.tool_sharpness to "sharpness",
        R.id.tool_noise_reduction to "noise_reduction",
        R.id.tool_exposure to "exposure",
        R.id.tool_saturation to "saturation",
        R.id.tool_warmth to "warmth",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ekran_redact)

        // --- Initialize Views ---
        setupViews()

        // Load the image
        loadImage()

        // --- Set Click Listeners ---
        btnNazad.setOnClickListener { onBackPressed() } // Use onBackPressed for standard back behavior
        btnSave.setOnClickListener { showSaveQualityDialog() }

        // Set click listeners for filter tools using the map
        filterToolMap.forEach { (viewId, filterName) ->
            findViewById<LinearLayout>(viewId).setOnClickListener { toggleSlider(filterName) }
        }

        // Destructive/Overlay Operations
        findViewById<LinearLayout>(R.id.tool_crop).setOnClickListener { enterEditingMode(EditingMode.CROP) }
        findViewById<LinearLayout>(R.id.tool_sketch).setOnClickListener { enterEditingMode(EditingMode.DRAWING) }
        findViewById<LinearLayout>(R.id.tool_text).setOnClickListener { showTextInputDialog() }
        findViewById<LinearLayout>(R.id.tool_neuro).setOnClickListener { neuronActivation(imageEditorView) }

        // Slider Listener
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentFilter?.let { filter ->
                    filterValues[filter] = progress
                    imageEditorView.setColorMatrix(getCombinedColorMatrix())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply/Cancel Listeners (Get references within setupViews)
        findViewById<Button>(R.id.btn_cancel_edit).setOnClickListener { cancelEditingMode() }
        findViewById<Button>(R.id.btn_apply_edit).setOnClickListener { applyEditingMode() }

        // Percentage Crop Listeners
        btnCropVertical.setOnClickListener { applyPercentageCrop(vertical = 0.10f) }
        btnCropHorizontal.setOnClickListener { applyPercentageCrop(horizontal = 0.10f) }
        btnCropAllSides.setOnClickListener { applyPercentageCrop(vertical = 0.10f, horizontal = 0.10f) }

        // Brush Control Listeners
        btnColorBlack.setOnClickListener { imageEditorView.setBrushColor(Color.BLACK) }
        btnColorRed.setOnClickListener { imageEditorView.setBrushColor(Color.RED) }
        btnColorGreen.setOnClickListener { imageEditorView.setBrushColor(Color.GREEN) }
        btnColorBlue.setOnClickListener { imageEditorView.setBrushColor(Color.BLUE) }

        brushSizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                imageEditorView.setBrushStrokeWidth(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Hide extra containers initially
        updateUIVisibility(EditingMode.NONE) // Call the helper to set initial visibility
    }

    private fun neuronActivation(iv: ImageEditorView) {
        val apiKey = "0d25d9b9313a46b9b66d6ce1b4dc8e8a" // Замените на реальный ключ от Cutout.pro

        // Получаем изображение из ImageView
        val bitmap = iv.drawToBitmap()
        try {
            // Конвертируем Bitmap в байтовый поток PNG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()

            // Создаем тело запроса с изображением
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "image.png",
                    imageBytes.toRequestBody("image/png".toMediaTypeOrNull(), 0, imageBytes.size)
                )
                .build()

            // Формируем запрос
            val request = Request.Builder()
                .url("https://www.cutout.pro/api/v1/matting?mattingType=6")
                .header("APIKEY", apiKey)
                .post(requestBody)
                .build()

            // Выполняем асинхронный запрос
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showError("Ошибка сети: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val body = response.body ?: throw IOException("Empty response body")
                            val bytes = body.bytes()
                            if (bytes.isEmpty()) {
                                throw IOException("Empty image data")
                            }

                            // Проверьте в логах размер полученных данных
                            Log.d("API_RESPONSE", "Received ${bytes.size} bytes")

                            val resultBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, )
                            if (resultBitmap == null) {
                                throw IOException("Failed to decode image")
                            }
                            updateImageView(resultBitmap)
                        } else {
                            val errorBody = response.body?.string() ?: "No error body"
                            showError("API Error ${response.code}: $errorBody")
                        }
                    } catch (e: Exception) {
                        showError("Processing error: ${e.message}")
                    }
                }
            })
        } catch (e: Exception) {
            showError("Ошибка обработки: ${e.message}")
        }
    }

    // Вспомогательные функции для работы с UI
    private fun updateImageView(bitmap: Bitmap) {
        runOnUiThread {
            findViewById<ImageEditorView>(R.id.imageEditorView).setBitmap(bitmap)
            Toast.makeText(this@ekran_redact, "Изображение обновлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this@ekran_redact, message, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Setup Views Helper ---
    private fun setupViews() {
        imageEditorView = findViewById(R.id.imageEditorView)
        btnNazad = findViewById(R.id.btn_nazad)
        btnSave = findViewById(R.id.btn_save)
        sliderContainer = findViewById(R.id.slider_container)
        slider = findViewById(R.id.slider)

        // Get parent for editing controls (assuming btn_cancel_edit is a child)
        editingControlsContainer = findViewById<Button>(R.id.btn_cancel_edit).parent as LinearLayout

        // Initialize other containers and UI elements
        cropOptionsContainer = findViewById(R.id.cropOptionsContainer)
        brushControlsContainer = findViewById(R.id.brushControlsContainer)
        brushSizeSlider = findViewById(R.id.brush_size_slider)
        btnColorBlack = findViewById(R.id.btn_color_black)
        btnColorRed = findViewById(R.id.btn_color_red)
        btnColorGreen = findViewById(R.id.btn_color_green)
        btnColorBlue = findViewById(R.id.btn_color_blue)

        // Initialize percentage crop buttons
        btnCropVertical = findViewById(R.id.btn_crop_vertical)
        btnCropHorizontal = findViewById(R.id.btn_crop_horizontal)
        btnCropAllSides = findViewById(R.id.btn_crop_all_sides)

        // Get reference to the main tools layout
        toolsLinearLayout = findViewById(R.id.toolsLinearLayout)
    }


    // --- Image Loading ---
    private fun loadImage() {
        intent.getStringExtra("imageUri")?.let { uriString ->
            try {
                val imageUri = Uri.parse(uriString)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                originalBitmap = bitmap
                currentBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                imageEditorView.setBitmap(currentBitmap)
                updateDisplay()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.failed_to_load_image), Toast.LENGTH_SHORT).show()
                imageEditorView.setBitmap(null)
            }
        } ?: run {
            // Load a default image if URI is null
            val defaultBitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_report_image)
            originalBitmap = defaultBitmap
            currentBitmap = defaultBitmap.copy(Bitmap.Config.ARGB_8888, true)
            imageEditorView.setBitmap(currentBitmap)
            updateDisplay()
        }
    }

    // --- Image Saving ---
    private fun showSaveQualityDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.save))
            .setMessage(getString(R.string.choose_save_quality))
            .setPositiveButton(getString(R.string.good_quality)) { _, _ -> saveImage(85) }
            .setNegativeButton(getString(R.string.low_quality)) { _, _ -> saveImage(0) }
            .show()
    }

    private fun saveImage(quality: Int) {
        currentBitmap?.let { bitmap ->
            try {
                val finalBitmapForSave = applyColorMatrixToBitmap(bitmap, getCombinedColorMatrix())
                val folderToSave: String = cacheDir.toString() // Consider using getExternalFilesDir or similar for persistent storage
                val file = File(folderToSave, generateUniqueName() + ".jpg")

                FileOutputStream(file).use { stream ->
                    finalBitmapForSave.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                }

                // Insert into MediaStore to make it visible in gallery
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    file.absolutePath,
                    file.name,
                    file.name
                )

                Toast.makeText(this, "Изображение сохранено ($quality)", Toast.LENGTH_SHORT).show()
                // Optionally start the next activity after save
                 startActivity(Intent(this, zaversheniye::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
    }

    private fun generateUniqueName(): String {
        val time = Time().apply { setToNow() }
        return "${time.year}${time.month}${time.monthDay}${time.hour}${time.minute}${time.second}"
    }

    // --- Overlay Management (Adds overlays and enters editing mode) ---
    private fun addTextOverlay(text: String) {
        currentBitmap?.let { baseBitmap ->
            val imgWidth = baseBitmap.width.toFloat()
            val imgHeight = baseBitmap.height.toFloat()

            // Define initial position and size relative to image bounds
            val initialWidth = imgWidth * 0.5f
            val initialHeight = initialWidth * 0.2f // Maintain aspect ratio or set differently
            val initialLeft = imgWidth * 0.25f
            val initialTop = imgHeight * 0.2f

            val bounds = RectF(initialLeft, initialTop, initialLeft + initialWidth, initialTop + initialHeight)

            val textOverlay = TextOverlay(
                bounds = bounds,
                text = text,
                textSize = 64f // Example default text size
            )
            imageEditorView.addOverlay(textOverlay)
            enterEditingMode(EditingMode.OVERLAY)
            Toast.makeText(this, getString(R.string.text_added_interactive_editing_needed), Toast.LENGTH_SHORT).show()
        }
    }

    // --- Editing Mode Management (Crop, Overlay, Drawing) ---
    private fun enterEditingMode(mode: EditingMode) {
        if (currentBitmap == null) {
            Toast.makeText(this, getString(R.string.no_image_loaded_to_edit), Toast.LENGTH_SHORT).show()
            return
        }

        // Apply previous changes if in another editing mode
        if (currentEditingMode != EditingMode.NONE && currentEditingMode != mode) {
            applyEditingMode()
            // Check if applyEditingMode successfully exited the previous mode
            if (currentEditingMode != EditingMode.NONE) {
                Toast.makeText(this, getString(R.string.finish_previous_edit_first), Toast.LENGTH_SHORT).show()
                return // Prevent entering the new mode
            }
        } else if (currentEditingMode == mode) {
            // Already in this mode, do nothing or show a message
            return
        }


        // Exit filter mode if active
        if (currentFilter != null) {
            currentFilter = null
            filterValues.clear()
            imageEditorView.setColorMatrix(null)
        }

        // Set the new mode
        currentEditingMode = mode
        imageEditorView.setEditingMode(mode)
        updateUIVisibility(mode) // Update UI based on the new mode

        when (mode) {
            EditingMode.CROP -> Toast.makeText(this, getString(R.string.crop_mode), Toast.LENGTH_SHORT).show()
            EditingMode.OVERLAY -> Toast.makeText(this, getString(R.string.overlay_mode), Toast.LENGTH_SHORT).show()
            EditingMode.DRAWING -> Toast.makeText(this, getString(R.string.drawing_mode), Toast.LENGTH_SHORT).show()
            EditingMode.NONE -> { /* Handled by UI visibility */ }
        }
        updateDisplay() // Ensure display reflects mode change (e.g., drawing guides)
    }

    private fun cancelEditingMode() {
        when (currentEditingMode) {
            EditingMode.CROP -> {
                imageEditorView.resetInteractionState()
                Toast.makeText(this, getString(R.string.crop_cancelled), Toast.LENGTH_SHORT).show()
            }
            EditingMode.OVERLAY -> {
                imageEditorView.resetInteractionState()
                Toast.makeText(this, getString(R.string.overlay_editing_cancelled), Toast.LENGTH_SHORT).show()
                // Overlays are kept in the list unless removed explicitly
            }
            EditingMode.DRAWING -> {
                imageEditorView.resetInteractionState() // Clear temporary drawing paths
                Toast.makeText(this, getString(R.string.drawing_cancelled), Toast.LENGTH_SHORT).show()
            }
            EditingMode.NONE -> { /* Should not happen */ }
        }
        // Always return to NONE mode after cancelling
        currentEditingMode = EditingMode.NONE
        imageEditorView.setEditingMode(EditingMode.NONE)
        updateUIVisibility(EditingMode.NONE)
        // Filters were already cleared when entering edit mode, no need to clear again here.
        updateDisplay()
    }

    private fun applyEditingMode() {
        currentBitmap?.let { bitmap ->
            when (currentEditingMode) {
                EditingMode.CROP -> {
                    // Percentage crop applied immediately by buttons, no logic here
                    Toast.makeText(this, getString(R.string.operation_applied), Toast.LENGTH_SHORT).show()
                }
                EditingMode.OVERLAY -> {
                    val overlaysToApply = imageEditorView.getOverlays()
                    if (overlaysToApply.isNotEmpty()) {
                        currentBitmap = applyOverlaysToBitmapPermanently(currentBitmap!!, overlaysToApply)
                        imageEditorView.setOverlays(emptyList()) // Clear overlays after applying
                        Toast.makeText(this, getString(R.string.overlays_applied), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.no_overlays_to_apply), Toast.LENGTH_SHORT).show()
                    }
                }
                EditingMode.DRAWING -> {
                    val pathsToApply = imageEditorView.getDrawingPathsImage()
                    if (pathsToApply.isNotEmpty()) {
                        currentBitmap = applyDrawingPathsToBitmapPermanently(currentBitmap!!, pathsToApply, imageEditorView.getDrawingPaintProperties())
                        imageEditorView.setDrawingPaths(emptyList()) // Clear paths after applying
                        Toast.makeText(this, getString(R.string.drawing_applied), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.no_drawing_to_apply), Toast.LENGTH_SHORT).show()
                    }
                }
                EditingMode.NONE -> { /* Should not happen */ }
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.no_image_loaded_to_apply_edits), Toast.LENGTH_SHORT).show()
        }

        // Always return to NONE mode after applying
        currentEditingMode = EditingMode.NONE
        imageEditorView.setEditingMode(EditingMode.NONE)
        imageEditorView.resetInteractionState()
        updateUIVisibility(EditingMode.NONE)
        updateDisplay()
    }

    // Helper to manage UI visibility based on EditingMode
    private fun updateUIVisibility(mode: EditingMode) {
        sliderContainer.visibility = View.GONE
        editingControlsContainer.visibility = View.GONE
        cropOptionsContainer.visibility = View.GONE
        brushControlsContainer.visibility = View.GONE
        toolsLinearLayout.visibility = View.GONE // Hide main tools by default

        when (mode) {
            EditingMode.NONE -> {
                toolsLinearLayout.visibility = View.VISIBLE
            }
            EditingMode.CROP -> {
                cropOptionsContainer.visibility = View.VISIBLE
                // editingControlsContainer can be shown here if "Cancel Crop" is desired
            }
            EditingMode.OVERLAY -> {
                editingControlsContainer.visibility = View.VISIBLE
            }
            EditingMode.DRAWING -> {
                brushControlsContainer.visibility = View.VISIBLE
                editingControlsContainer.visibility = View.VISIBLE
            }
        }

        // Special case: if slider is active, show it regardless of editing mode (but it's usually only active in NONE)
        if (currentFilter != null && mode == EditingMode.NONE) {
            sliderContainer.visibility = View.VISIBLE
        }
    }


    // --- Percentage Crop Logic ---
    private fun applyPercentageCrop(vertical: Float = 0f, horizontal: Float = 0f) {
        currentBitmap?.let { bitmap ->
            if (vertical == 0f && horizontal == 0f) return

            val currentWidth = bitmap.width
            val currentHeight = bitmap.height

            val cropLeft = (currentWidth * horizontal).toInt()
            val cropTop = (currentHeight * vertical).toInt()
            val cropRight = currentWidth - (currentWidth * horizontal).toInt()
            val cropBottom = currentHeight - (currentHeight * vertical).toInt()

            if (cropRight > cropLeft && cropBottom > cropTop) {
                try {
                    val croppedBitmap = Bitmap.createBitmap(
                        bitmap,
                        cropLeft,
                        cropTop,
                        cropRight - cropLeft, // Use width
                        cropBottom - cropTop  // Use height
                    )
                    currentBitmap = croppedBitmap
                    imageEditorView.setOverlays(emptyList()) // Clear overlays/paths
                    imageEditorView.setDrawingPaths(emptyList())
                    imageEditorView.setBitmap(currentBitmap)
                    updateDisplay()
                    Toast.makeText(this, getString(R.string.image_cropped), Toast.LENGTH_SHORT).show()
                    // After applying, exit CROP mode
                    enterEditingMode(EditingMode.NONE) // Use enterEditingMode to reset UI
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.operation_failed, e.message), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_crop_rectangle), Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, getString(R.string.no_image_loaded_to_edit), Toast.LENGTH_SHORT).show()
    }

    // --- Drawing and Display Logic ---

    // Function to update CustomImageEditorView based on current state
    private fun updateDisplay() {
        currentBitmap?.let { bitmap ->
            imageEditorView.setBitmap(bitmap)
            imageEditorView.setColorMatrix(getCombinedColorMatrix())
            imageEditorView.invalidate()
        } ?: run {
            imageEditorView.setBitmap(null)
            imageEditorView.setColorMatrix(null)
            imageEditorView.invalidate()
        }
    }

    // Apply overlays (only TextOverlay) permanently to a bitmap
    private fun applyOverlaysToBitmapPermanently(baseBitmap: Bitmap, overlaysToDraw: List<ImageOverlay>): Bitmap {
        val textOverlaysToDraw = overlaysToDraw.filterIsInstance<TextOverlay>()
        if (textOverlaysToDraw.isEmpty()) return baseBitmap

        val resultBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        textOverlaysToDraw.forEach { overlay ->
            val paint = Paint().apply {
                color = overlay.textColor
                textSize = overlay.textSize
                isAntiAlias = true
            }
            canvas.save()
            canvas.rotate(overlay.rotation, overlay.bounds.centerX(), overlay.bounds.centerY())

            val fm = paint.fontMetrics
            val textBaselineY = overlay.bounds.top - fm.ascent + (overlay.bounds.height() - (fm.descent - fm.ascent)) / 2f

            canvas.drawText(overlay.text, overlay.bounds.left, textBaselineY, paint)
            canvas.restore()
        }
        return resultBitmap
    }

    // Apply drawing paths permanently to a bitmap
    private fun applyDrawingPathsToBitmapPermanently(baseBitmap: Bitmap, pathsToDraw: List<Path>, paint: Paint): Bitmap {
        if (pathsToDraw.isEmpty()) return baseBitmap

        val resultBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        pathsToDraw.forEach { path ->
            canvas.drawPath(path, paint)
        }
        return resultBitmap
    }

    private fun applyColorMatrixToBitmap(bitmap: Bitmap, matrix: ColorMatrix?): Bitmap {
        if (matrix == null) {
            // Corrected line
            return bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    // --- Slider and Filter Logic ---

    // Calculates the combined ColorMatrix from all active filters
    private fun getCombinedColorMatrix(): ColorMatrix? {
        if (filterValues.isEmpty()) return null

        val combinedMatrix = ColorMatrix()

        filterValues.forEach { (filter, value) ->
            val filterMatrix = ColorMatrix()
            when (filter) {
                "brightness" -> {
                    val brightness = (value.toFloat() - 50.0f) * 2.55f
                    filterMatrix.set( floatArrayOf( 1f, 0f, 0f, 0f, brightness, 0f, 1f, 0f, 0f, brightness, 0f, 0f, 1f, 0f, brightness, 0f, 0f, 0f, 1f, 0f ))
                }
                "contrast" -> {
                    val contrast = value.toFloat() / 50.0f
                    val translate = (-.5f * contrast + .5f) * 255f
                    filterMatrix.set( floatArrayOf( contrast, 0f, 0f, 0f, translate, 0f, contrast, 0f, 0f, translate, 0f, 0f, contrast, 0f, translate, 0f, 0f, 0f, 1f, 0f ))
                }
                "sharpness" -> {
                    val sharpnessFactor = value.toFloat() / 100.0f
                    // Note: This is a simple unsharp mask like matrix, might need tuning
                    filterMatrix.set( floatArrayOf( 1 + sharpnessFactor, -sharpnessFactor / 3f, -sharpnessFactor / 3f, 0f, 0f, -sharpnessFactor / 3f, 1 + sharpnessFactor, -sharpnessFactor / 3f, 0f, 0f, -sharpnessFactor / 3f, -sharpnessFactor / 3f, 1 + sharpnessFactor, 0f, 0f, 0f, 0f, 0f, 1f, 0f ))
                }
                "noise_reduction" -> {
                    val strength = value.toFloat() / 100.0f
                    // Note: This matrix logic seems potentially complex/custom for noise reduction
                    filterMatrix.set( floatArrayOf( (1f - strength) * 0.8f + strength * 0.33f, (1f - strength) * 0.1f + strength * 0.33f, (1f - strength) * 0.1f + strength * 0.33f, 0f, 0f, strength * 0.33f, (1f - strength) * 0.8f + strength * 0.33f, strength * 0.33f, 0f, 0f, strength * 0.33f, strength * 0.1f + strength * 0.33f, (1f - strength) * 0.8f + strength * 0.33f, 0f, 0f, 0f, 0f, 0f, 1f, 0f ))
                    val desaturationMatrix = ColorMatrix().apply { setSaturation(1.0f - strength * 0.3f) }
                    filterMatrix.postConcat(desaturationMatrix)
                }
                "exposure" -> {
                    val exposure = value.toFloat() / 50.0f
                    filterMatrix.setScale(exposure, exposure, exposure, 1f)
                }
                "saturation" -> {
                    val saturation = value.toFloat() / 100.0f
                    filterMatrix.setSaturation(saturation)
                }
                "warmth" -> {
                    val warmth = (value.toFloat() - 50.0f) / 100.0f
                    // Note: This matrix logic for warmth might need tuning
                    filterMatrix.set( floatArrayOf( 1f + warmth, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f - warmth, 0f, 0f, 0f, 0f, 0f, 1f, 0f ))
                }
            }
            combinedMatrix.postConcat(filterMatrix)
        }
        return combinedMatrix
    }

    private fun toggleSlider(filter: String) {
        // If currently in an editing mode, cancel it first
        if (currentEditingMode != EditingMode.NONE) {
            cancelEditingMode() // This will also update UI visibility
        }

        if (currentFilter == filter) {
            // If clicking the same filter, hide slider and reset
            currentFilter = null
            filterValues.remove(filter)
            updateUIVisibility(EditingMode.NONE) // Show main tools
        } else {
            // If clicking a different filter or turning one on
            currentFilter = filter
            updateUIVisibility(EditingMode.NONE) // Ensure slider is visible and other modes hidden
            sliderContainer.visibility = View.VISIBLE // Explicitly show slider container

            setupSliderForFilter(filter)

            val value = filterValues[filter] ?: getDefaultValue(filter)
            slider.progress = value

            // filterValues[filter] = value // This is set in onProgressChanged now
        }
        updateDisplay() // Update image view to reflect filter changes
    }


    private fun setupSliderForFilter(filter: String) {
        when (filter) {
            "brightness", "contrast", "sharpness", "noise_reduction", "exposure", "warmth" -> slider.max = 100
            "saturation" -> slider.max = 200
            else -> slider.max = 100
        }
        // Ensure progress is within the new max range
        slider.progress = minOf(slider.progress, slider.max)
    }

    private fun getDefaultValue(filter: String): Int {
        return when (filter) {
            "brightness" -> 50
            "contrast" -> 50
            "sharpness" -> 0
            "noise_reduction" -> 0
            "exposure" -> 50
            "saturation" -> 100
            "warmth" -> 50
            else -> 50
        }
    }

    // --- Text Input Dialog ---
    private fun showTextInputDialog() {
        if (currentBitmap == null) {
            Toast.makeText(this, getString(R.string.no_image_loaded), Toast.LENGTH_SHORT).show()
            return
        }

        // Apply previous changes if in another editing mode
        if (currentEditingMode != EditingMode.NONE) {
            applyEditingMode()
            // Check if applyEditingMode successfully exited the previous mode
            if (currentEditingMode != EditingMode.NONE) {
                Toast.makeText(this, getString(R.string.finish_previous_edit_first), Toast.LENGTH_SHORT).show()
                return // Prevent showing dialog
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.add_text))
        val input = EditText(this)
        input.setPadding(50, 30, 50, 30)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.apply)) { dialog, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                addTextOverlay(text) // This will enter OVERLAY mode
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // --- Handle Back Press ---
    override fun onBackPressed() {
        // TODO: Add "Save changes?" dialog here if there are pending edits (currentBitmap != originalBitmap or overlays/paths exist)
        // For now, just go back
        startActivity(Intent(this, profil_menu::class.java))
        finish()
        // Alternatively, if you want to handle unsaved changes:
        /*
        if (hasUnsavedChanges()) { // Implement this check
            AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to discard them and exit?")
                .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            super.onBackPressed()
        }
        */
    }

    // TODO: Implement hasUnsavedChanges() based on comparing currentBitmap, overlays, and drawing paths
    /*
    private fun hasUnsavedChanges(): Boolean {
        // This is a complex check and would require deep comparison of Bitmaps,
        // and checking if overlays or drawing paths lists are not empty and differ
        // from the state when the image was last saved or loaded.
        // A simpler approach might be to track a 'isDirty' flag.
        return false // Placeholder
    }
     */

}