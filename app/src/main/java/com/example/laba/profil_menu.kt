package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class profil_menu : AppCompatActivity() {
    lateinit var domoy_test_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_menu)

        domoy_test_button = findViewById(R.id.domoy_test_button)

        domoy_test_button.setOnClickListener {
            val intent = Intent(this, vhod_ili_propusk::class.java)
            startActivity(intent)
        }

    }
}
