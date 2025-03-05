package com.example.laba

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var registr_email: EditText
    lateinit var registr_password: EditText
    lateinit var registr_button: Button
    private lateinit var shared_preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_reg)

        registr_name = findViewById(R.id.registr_name)
        registr_email = findViewById(R.id.registr_email)
        registr_password = findViewById(R.id.registr_password)
        registr_button = findViewById(R.id.registr_button)
        shared_preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        registr_button.setOnClickListener {

            val name = registr_name.text.toString().trim()
            val password = registr_password.text.toString().trim()

            val editor = shared_preferences.edit()
            editor.putString("EXTRA_NAME", name)
            editor.putString("EXTRA_PASSWORD", password)
            editor.apply()

            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }


    }
}

