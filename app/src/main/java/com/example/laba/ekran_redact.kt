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
import android.widget.ImageView
import android.widget.Toast
import java.io.IOException

class ekran_redact : AppCompatActivity() {
    private lateinit var izobrazheniye: ImageView
    private lateinit var btn_nazad: Button
    private lateinit var btn_save: Button
    private lateinit var btn_neuron: Button
    private lateinit var sliderContainer: LinearLayout
    private val activeFilters = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ekran_redact)

        izobrazheniye = findViewById(R.id.izobrazheniye)
        btn_nazad = findViewById(R.id.btn_nazad)
        btn_save = findViewById(R.id.btn_save)
        btn_neuron = findViewById(R.id.neuron)
        sliderContainer = findViewById(R.id.slider_container)

        intent.getStringExtra("imageUri")?.let {
            izobrazheniye.setImageURI(Uri.parse(it))
        } ?: izobrazheniye.setImageResource(android.R.drawable.ic_menu_report_image)

        btn_nazad.setOnClickListener {
            startActivity(Intent(this, glavniy_ekran::class.java))
            finish()
        }
        btn_neuron.setOnClickListener {
            neuronActivation(izobrazheniye)
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
            R.id.btn_function1 to "Фильтр 1",
            R.id.btn_function2 to "Фильтр 2",
            R.id.btn_function3 to "Фильтр 3",
            R.id.btn_function4 to "Фильтр 4",
            R.id.btn_function5 to "Фильтр 5",
            R.id.btn_function6 to "Фильтр 6"
        ).forEach { (buttonId, filter) ->
            findViewById<Button>(buttonId).setOnClickListener { toggleSlider(filter) }
        }
    }

    private fun toggleSlider(filter: String) {
        if (activeFilters.contains(filter)) {
            sliderContainer.findViewWithTag<View>(filter)?.let { sliderContainer.removeView(it) }
            activeFilters.remove(filter)
        } else {
            layoutInflater.inflate(R.layout.slider_layout, sliderContainer, false).apply {
                findViewById<TextView>(R.id.slider_label).text = "Настройка: $filter"
                tag = filter
                sliderContainer.addView(this, 0)
            }
            activeFilters.add(filter)
        }
        sliderContainer.visibility = if (activeFilters.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun neuronActivation(iv: ImageView) {
        // API ключ (замените на ваш реальный ключ)
        val apiKey = ""
        val prompt = "Make the image look like a cartoon"

        // Создаем тело запроса
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("prompt", prompt) // Промпт для генерации изображения
            .addFormDataPart("output_format", "jpeg") // Формат выходного изображения
            .build()

        // Создаем запрос
        val request = Request.Builder()
            .url("https://api.stability.ai/v2beta/stable-image/generate/sd3") // URL API
            .header("Authorization", "Bearer $apiKey") // Авторизация
            .header("Accept", "image/*") // Заголовок для принятия изображения
            .post(requestBody)
            .build()

        // Выполняем запрос
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки
                runOnUiThread {
                    Toast.makeText(this@ekran_redact, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Получаем байты изображения из ответа
                    val imageBytes = response.body?.bytes()

                    // Преобразуем байты в Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes?.size ?: 0)

                    // Устанавливаем Bitmap в ImageView
                    runOnUiThread {
                        iv.setImageBitmap(bitmap)
                        Toast.makeText(this@ekran_redact, "Изображение успешно загружено", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Обработка неуспешного ответа
                    runOnUiThread {
                        Toast.makeText(this@ekran_redact, "Ошибка: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun saveToaster(quality: Int, image: ImageView) {
        val folderToSave: String = cacheDir.toString()
        saveToFolder(quality, image, folderToSave)
        Toast.makeText(this, "Изображение сохранено ($quality)", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, zaversheniye::class.java))
    }

    private fun saveToFolder(quality: Int, iv: ImageView, folderToSave: String): String? {
        var fOut: OutputStream? = null
        val time: Time = Time()
        time.setToNow()

        try {
            val file = File(
                folderToSave,
                time.year.toString() + time.month.toString() + time.monthDay.toString() + time.hour.toString() + time.minute.toString() + time.second.toString() + ".jpg"
            ) // создать уникальное имя для файла основываясь на дате сохранения
            fOut = FileOutputStream(file)
            val bitmap: Bitmap = iv.drawable.toBitmap()
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                fOut
            ) // сохранять картинку в jpeg-формате с 85% сжатия.
            fOut.flush()
            fOut.close()
            MediaStore.Images.Media.insertImage(
                contentResolver,
                file.absolutePath,
                file.name,
                file.name
            ) // регистрация в фотоальбоме
        } catch (e: Exception) // здесь необходим блок отслеживания реальных ошибок и исключений, общий Exception приведен в качестве примера
        {
            return e.message
        }
        return ""
    }
}
