package com.example.laba

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Time
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.ImageView
import android.widget.Toast
import java.io.IOException
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.SeekBar
import android.widget.ImageButton

import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton

class ekran_redact : AppCompatActivity() {
    private lateinit var izobrazheniye: ImageView
    private lateinit var btn_nazad: ImageButton
    private lateinit var btn_save: ImageButton
    private lateinit var btn_neuron: ImageButton
    private lateinit var sliderContainer: LinearLayout
    private lateinit var slider: SeekBar
    private val activeFilters = mutableSetOf<String>()
    private val filterValues = mutableMapOf<String, Int>()
    private var currentFilter: String? = null // Отслеживаем текущий активный фильтр


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ekran_redact)
        slider = findViewById(R.id.slider)
        izobrazheniye = findViewById(R.id.izobrazheniye)
        btn_nazad = findViewById(R.id.btn_nazad)
        btn_save = findViewById(R.id.btn_save)
        btn_neuron = findViewById(R.id.neuron)
        sliderContainer = findViewById(R.id.slider_container)

        intent.getStringExtra("imageUri")?.let {
            izobrazheniye.setImageURI(Uri.parse(it))
        } ?: izobrazheniye.setImageResource(android.R.drawable.ic_menu_report_image)

        btn_nazad.setOnClickListener {
            startActivity(Intent(this, profil_menu::class.java))
            finish()
        }
        btn_neuron.setOnClickListener {
     //       neuronActivation(izobrazheniye)
        }
        btn_save.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Сохранение изображения")
                .setMessage("Выберите качество сохранения:")
                .setPositiveButton("Хорошее качество") { _, _ -> saveToaster(85, izobrazheniye) }
                .setNegativeButton("Плохое качество") { _, _ -> saveToaster(0, izobrazheniye) }
                .show()
        }

        listOf(
            R.id.btn_function1 to "brightness",
            R.id.btn_function2 to "contrast",
            R.id.btn_function3 to "sharpness",
            R.id.btn_function4 to "noise_reduction",
            R.id.btn_function5 to "Фильтр 5",
            R.id.btn_function6 to "Фильтр 6"
        ).forEach { (buttonId, filter) ->
            findViewById<ImageButton>(buttonId).setOnClickListener { toggleSlider(filter) }
        }
    }

    private fun toggleSlider(filter: String) {
        if (currentFilter == filter) {
            // Деактивируем текущий фильтр
            currentFilter = null
            sliderContainer.visibility = View.GONE
            applyFilter(filter, getDefaultValue(filter))
        } else {
            // Деактивируем предыдущий фильтр (если есть)
            currentFilter?.let { oldFilter ->
                applyFilter(oldFilter, getDefaultValue(oldFilter))
            }
            // Активируем новый фильтр
            currentFilter = filter
            sliderContainer.visibility = View.VISIBLE

            // Настраиваем параметры слайдера в зависимости от фильтра
            setupSliderForFilter(filter)

            // Устанавливаем значение слайдера из filterValues или стандартное
            val value = filterValues[filter] ?: getDefaultValue(filter)
            slider.progress = value

            // Обновляем значение при изменении слайдера
            slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    filterValues[filter] = progress
                    applyFilter(filter, progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            applyFilter(filter, value)
        }
    }

    // Настройка параметров слайдера для конкретного фильтра
    private fun setupSliderForFilter(filter: String) {
        when (filter) {
            "contrast" -> {
                slider.max = 100 // Пример диапазона для контраста
            }
            // Добавьте настройки для других фильтров
            else -> {
                slider.max = 100
            }
        }
    }

    // Стандартные значения для каждого фильтра
    private fun getDefaultValue(filter: String): Int {
        return when (filter) {
            "contrast" -> 50 // Среднее значение для контраста
            else -> 50
        }
    }

    // Применение фильтра к изображению (заглушка, реализуйте под свои нужды)
    private fun applyFilter(filter: String, value: Int) {
        val imageView = izobrazheniye
        when (filter) {
            "contrast" -> {
                val contrast = value / 100.0f
                val matrix = ColorMatrix().apply { setScale(contrast, contrast, contrast, 1f) }
                imageView.colorFilter = ColorMatrixColorFilter(matrix)
            }
            "brightness" -> {
                val brightness = value - 50
                val matrix = ColorMatrix().apply {
                    setScale(1f, 1f, 1f, 1f)
                    postConcat(
                        ColorMatrix(
                            floatArrayOf(
                                1f, 0f, 0f, 0f, brightness.toFloat(),
                                0f, 1f, 0f, 0f, brightness.toFloat(),
                                0f, 0f, 1f, 0f, brightness.toFloat(),
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                    )
                }
                imageView.colorFilter = ColorMatrixColorFilter(matrix)
            }
            "sharpness" -> {
                // Упрощенная реализация через усиление контраста
                val sharpnessFactor = 1.0f + (value / 200.0f)
                val matrix = ColorMatrix(
                    floatArrayOf(
                        sharpnessFactor, 0f, 0f, 0f, 0f,
                        0f, sharpnessFactor, 0f, 0f, 0f,
                        0f, 0f, sharpnessFactor, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                imageView.colorFilter = ColorMatrixColorFilter(matrix)
            }
            "noise_reduction" -> {
                // Упрощенная реализация через снижение контраста
                val noiseReduction = 1.0f - (value / 400.0f)
                val matrix = ColorMatrix().apply { setScale(noiseReduction, noiseReduction, noiseReduction, 1f) }
                imageView.colorFilter = ColorMatrixColorFilter(matrix)
            }
        }
    }

//    private fun neuronActivation(iv: ImageView) {
//        // API ключ (замените на ваш реальный ключ)
//        val apiKey = ""
//        val prompt = "Make the image look like a cartoon"
//
//        // Создаем тело запроса
//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("prompt", prompt) // Промпт для генерации изображения
//            .addFormDataPart("output_format", "jpeg") // Формат выходного изображения
//            .build()
//
//        // Создаем запрос
//        val request = Request.Builder()
//            .url("https://api.stability.ai/v2beta/stable-image/generate/sd3") // URL API
//            .header("Authorization", "Bearer $apiKey") // Авторизация
//            .header("Accept", "image/*") // Заголовок для принятия изображения
//            .post(requestBody)
//            .build()
//
//        // Выполняем запрос
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Обработка ошибки
//                runOnUiThread {
//                    Toast.makeText(this@ekran_redact, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    // Получаем байты изображения из ответа
//                    val imageBytes = response.body?.bytes()
//
//                    // Преобразуем байты в Bitmap
//                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes?.size ?: 0)
//
//                    // Устанавливаем Bitmap в ImageView
//                    runOnUiThread {
//                        iv.setImageBitmap(bitmap)
//                        Toast.makeText(this@ekran_redact, "Изображение успешно загружено", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    // Обработка неуспешного ответа
//                    runOnUiThread {
//                        Toast.makeText(this@ekran_redact, "Ошибка: ${response.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        })
//    }

    private fun saveToaster(quality: Int, image: ImageView) {
        val folderToSave: String = cacheDir.toString()
        saveToFolder(quality, folderToSave)
        Toast.makeText(this, "Изображение сохранено ($quality)", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, zaversheniye::class.java))
    }

    private fun applyFiltersToBitmap(bitmap: Bitmap): Bitmap {
        var result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        // Применяем все активные фильтры
        filterValues.forEach { (filter, value) ->
            when (filter) {
                "contrast" -> {
                    val contrast = value / 100f // Пример: 100 = 1.0f (оригинал)
                    val matrix = ColorMatrix().apply { setScale(contrast, contrast, contrast, 1f) }
                    paint.colorFilter = ColorMatrixColorFilter(matrix)
                    canvas.drawBitmap(result, 0f, 0f, paint)
                }
                "brightness" -> {
                    val brightness = value - 50 // Пример: [-50..50]
                    val matrix = ColorMatrix().apply {
                        setScale(1f, 1f, 1f, 1f)
                        postConcat(
                            ColorMatrix(
                                floatArrayOf(
                                    1f, 0f, 0f, 0f, brightness.toFloat(),
                                    0f, 1f, 0f, 0f, brightness.toFloat(),
                                    0f, 0f, 1f, 0f, brightness.toFloat(),
                                    0f, 0f, 0f, 1f, 0f
                                )
                            )
                        )
                    }
                    paint.colorFilter = ColorMatrixColorFilter(matrix)
                    canvas.drawBitmap(result, 0f, 0f, paint)
                }
                // Добавьте другие фильтры по аналогии
            }
        }
        return result
    }

    private fun saveToFolder(quality: Int, folderToSave: String): String? {
        return try {
            val filteredBitmap = applyFiltersToBitmap(izobrazheniye.drawable.toBitmap()) // Применяем фильтры
            val file = File(folderToSave, generateUniqueName() + ".jpg")

            FileOutputStream(file).use { stream ->
                filteredBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            MediaStore.Images.Media.insertImage(
                contentResolver,
                file.absolutePath,
                file.name,
                file.name
            )
            ""
        } catch (e: Exception) {
            e.message
        }
    }

    private fun generateUniqueName(): String {
        val time = Time().apply { setToNow() }
        return "${time.year}${time.month}${time.monthDay}${time.hour}${time.minute}${time.second}"
    }

}
