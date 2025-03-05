package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.content.SharedPreferences

class profil_menu : AppCompatActivity() {
    private lateinit var domoy_test_button: Button
    private lateinit var prosmotr_logina: TextView
    private lateinit var prosmotr_parolya: TextView
    private lateinit var shared_preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_menu)

        domoy_test_button = findViewById(R.id.domoy_test_button)
        prosmotr_logina = findViewById(R.id.prosmotr_logina)
        prosmotr_parolya = findViewById(R.id.prosmotr_parolya)

        shared_preferences = getSharedPreferences("dannie_profilya", MODE_PRIVATE)
        val name = shared_preferences.getString("EXTRA_NAME", "")
        val password = shared_preferences.getString("EXTRA_PASSWORD", "")

        prosmotr_logina.text = "Логин: $name"
        prosmotr_parolya.text = "Пароль: $password"

        domoy_test_button.setOnClickListener {
            val intent = Intent(this, vhod_ili_propusk::class.java)
            startActivity(intent)
        }
    }
}
