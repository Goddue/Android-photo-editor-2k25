package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class vhod_ili_reg : AppCompatActivity() {
    lateinit var vhod_vibor: Button
    lateinit var registr_vibor: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vhod_ili_reg)

        vhod_vibor = findViewById(R.id.vhod_vibor)
        registr_vibor = findViewById(R.id.registr_vibor)

        vhod_vibor.setOnClickListener {
            val intent = Intent(this, okno_vhoda::class.java)
            startActivity(intent)
        }

        registr_vibor.setOnClickListener {
            val intent = Intent(this, okno_reg::class.java)
            startActivity(intent)
        }
    }
}
