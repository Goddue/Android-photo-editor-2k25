package com.example.laba

import android.content.Intent
<<<<<<< HEAD
import android.content.SharedPreferences
=======
>>>>>>> abcc495 (Add NSGs work)
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
<<<<<<< HEAD
import android.widget.Toast

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var parol_perviy: EditText
    lateinit var parol_povtor: EditText
    lateinit var registr_button: Button
    private lateinit var shared_preferences: SharedPreferences
=======

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var registr_password: EditText
    lateinit var registr_povt_password: EditText
    lateinit var registr_button: Button
>>>>>>> abcc495 (Add NSGs work)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_reg)

        registr_name = findViewById(R.id.registr_name)
<<<<<<< HEAD
        parol_perviy = findViewById(R.id.parol_perviy)
        parol_povtor = findViewById(R.id.parol_povtor)
        registr_button = findViewById(R.id.registr_button)
        shared_preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        registr_button.setOnClickListener {

            val name = registr_name.text.toString().trim()
            val password_perviy = parol_perviy.text.toString().trim()
            val password = parol_povtor.text.toString().trim()

            if (password_perviy == password){
                Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                val editor = shared_preferences.edit()
                editor.putString("EXTRA_NAME", name)
                editor.putString("EXTRA_PASSWORD", password)
                editor.apply()

                val intent = Intent(this, glavniy_ekran::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Пароли должны совпадать", Toast.LENGTH_SHORT).show()
            }
=======
        registr_password = findViewById(R.id.registr_password)
        registr_povt_password = findViewById(R.id.registr_povt_password)
        registr_button = findViewById(R.id.registr_button)

        registr_button.setOnClickListener {
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
>>>>>>> abcc495 (Add NSGs work)
        }


    }
}

