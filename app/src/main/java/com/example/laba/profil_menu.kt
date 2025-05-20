package com.example.laba

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.content.SharedPreferences
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class profil_menu : AppCompatActivity() {
    private lateinit var domoy_test_button: Button
    private lateinit var prosmotr_logina: TextView
    private lateinit var prosmotr_parolya: TextView
    private lateinit var shared_preferences: SharedPreferences
    private lateinit var avatar_image: ImageView
    private lateinit var avtorizovatsya_button: Button
    private lateinit var vernutsya_button: Button
    private lateinit var vyyti_button: Button

    // Контракт для выбора изображения из галереи
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Сохраняем URI выбранного изображения
            shared_preferences.edit().putString("AVATAR_URI", it.toString()).apply()
            avatar_image.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_menu)

        domoy_test_button = findViewById(R.id.domoy_test_button)
        prosmotr_logina = findViewById(R.id.prosmotr_logina)
        prosmotr_parolya = findViewById(R.id.prosmotr_parolya)
        avatar_image = findViewById(R.id.avatar_image)
        avtorizovatsya_button = findViewById(R.id.avtorizovatsya_button)
        vernutsya_button = findViewById(R.id.vernutsya_button)
        vyyti_button = findViewById(R.id.vyyti_button)

        shared_preferences = getSharedPreferences("dannie_profilya", MODE_PRIVATE)
        val name = shared_preferences.getString("EXTRA_NAME", "")
        val password = shared_preferences.getString("EXTRA_PASSWORD", "")
        val avatarUri = shared_preferences.getString("AVATAR_URI", null)

        if (name.isNullOrEmpty() || password.isNullOrEmpty()) {
            // Пользователь не вошел
            avatar_image.setImageResource(R.drawable.gray_avatar)
            avatar_image.setOnClickListener(null)
            prosmotr_logina.visibility = View.GONE
            prosmotr_parolya.visibility = View.GONE
            vyyti_button.visibility = View.GONE
            domoy_test_button.visibility = View.GONE

            avtorizovatsya_button.setOnClickListener {
                val intent = Intent(this, vhod_ili_reg::class.java)
                startActivity(intent)
            }
        } else {
            // Пользователь вошел
            if (avatarUri != null) {
                avatar_image.setImageURI(Uri.parse(avatarUri))
            } else {
                avatar_image.setImageResource(R.drawable.default_avatar)
            }

            avatar_image.setOnClickListener {
                selectImage.launch("image/*")
            }

            prosmotr_logina.text = "Логин: $name"
            prosmotr_parolya.text = "Пароль: $password"
            avtorizovatsya_button.visibility = View.GONE

            vyyti_button.setOnClickListener {
                shared_preferences.edit().clear().apply()
                recreate() // Перезагружаем активити
            }
        }

        vernutsya_button.setOnClickListener {
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }

        domoy_test_button.setOnClickListener {
            val intent = Intent(this, vhod_ili_propusk::class.java)
            startActivity(intent)
        }
    }
}