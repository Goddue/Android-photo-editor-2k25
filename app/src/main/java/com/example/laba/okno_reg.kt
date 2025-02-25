package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var registr_password: EditText
    lateinit var registr_povt_password: EditText
    lateinit var registr_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_reg)

        registr_name = findViewById(R.id.registr_name)
        registr_password = findViewById(R.id.registr_password)
        registr_povt_password = findViewById(R.id.registr_povt_password)
        registr_button = findViewById(R.id.registr_button)

        registr_button.setOnClickListener {
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }


    }
}

