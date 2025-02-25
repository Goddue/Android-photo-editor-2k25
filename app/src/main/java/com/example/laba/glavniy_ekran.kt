package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glavniy_ekran)

        pereh_v_profil_button = findViewById(R.id.pereh_v_profil_button)

        pereh_v_profil_button.setOnClickListener {
            val intent = Intent(this, profil_menu::class.java)
            startActivity(intent)
        }

    }
}
