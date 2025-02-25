package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class vhod_ili_propusk : AppCompatActivity() {
    lateinit var vhod_vibor_ekr: Button
    lateinit var propusk_vibor_ekr: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vhod_ili_propusk)

        vhod_vibor_ekr = findViewById(R.id.vhod_vibor_ekr)
        propusk_vibor_ekr = findViewById(R.id.propusk_vibor_ekr)

        vhod_vibor_ekr.setOnClickListener {
            val intent = Intent(this, vhod_ili_reg::class.java)
            startActivity(intent)
        }

        propusk_vibor_ekr.setOnClickListener {
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }
    }
}
