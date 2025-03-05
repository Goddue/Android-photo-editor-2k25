package com.example.laba

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class zaversheniye : AppCompatActivity() {
    private lateinit var rating_container: LinearLayout
    private lateinit var btn_predlozh_otsenku: Button
    private lateinit var btn_otpravka: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_zaversheniye)

        rating_container = findViewById(R.id.rating_container)
        btn_predlozh_otsenku = findViewById(R.id.predlozh_otsenku_button)
        btn_otpravka = findViewById(R.id.btn_otpravka)

        rating_container.visibility = View.GONE
        btn_predlozh_otsenku.setOnClickListener {
            rating_container.visibility = View.VISIBLE
        }

        btn_otpravka.setOnClickListener {
            Toast.makeText(this, "Ты лучший!", Toast.LENGTH_SHORT).show()
        }
    }
}
