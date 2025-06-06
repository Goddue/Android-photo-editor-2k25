package com.example.laba

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var parol_perviy: EditText
    lateinit var parol_povtor: EditText
    lateinit var registr_button: Button
    private lateinit var shared_preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_reg)

        registr_name = findViewById(R.id.registr_name)
        parol_perviy = findViewById(R.id.parol_perviy)
        parol_povtor = findViewById(R.id.parol_povtor)
        registr_button = findViewById(R.id.registr_button)
        shared_preferences = getSharedPreferences("dannie_profilya", MODE_PRIVATE)

        registr_button.setOnClickListener {
            val name = registr_name.text.toString()
            val password_perviy = parol_perviy.text.toString()
            val password = parol_povtor.text.toString()

            if (password_perviy == password) {
                val slovarJson = shared_preferences.getString("slovar_polzovateley", "{}")
                val slovar = JSONObject(slovarJson)

                if (slovar.has(name)) {
                    Toast.makeText(this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show()
                } else {
                    slovar.put(name, password)
                    shared_preferences.edit().putString("slovar_polzovateley", slovar.toString()).apply()

                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()

                    shared_preferences.edit().putString("EXTRA_NAME", name).putString("EXTRA_PASSWORD", password).apply()

                    val intent = Intent(this, glavniy_ekran::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Пароли должны совпадать", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
