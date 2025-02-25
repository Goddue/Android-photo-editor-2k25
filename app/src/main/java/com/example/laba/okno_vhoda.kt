package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class okno_vhoda : AppCompatActivity() {
    lateinit var vhod_name: EditText
    lateinit var vhod_password: EditText
    lateinit var vhod_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_vhoda)

        vhod_name = findViewById(R.id.vhod_name)
        vhod_password = findViewById(R.id.vhod_password)
        vhod_button = findViewById(R.id.vhod_button)

        vhod_button.setOnClickListener {
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }


        }
    }

