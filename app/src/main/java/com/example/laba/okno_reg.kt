package com.example.laba

import android.content.Intent
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b4b977d (Fix for last master commit)
import android.content.SharedPreferences
=======
>>>>>>> abcc495 (Add NSGs work)
=======
import android.content.SharedPreferences
>>>>>>> 66fe6b8 (Fix for last master commit)
<<<<<<< HEAD
=======
import android.content.SharedPreferences
=======
>>>>>>> abcc495 (Add NSGs work)
>>>>>>> 09b46dd (Add NSGs work)
=======
>>>>>>> b4b977d (Fix for last master commit)
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
<<<<<<< HEAD
<<<<<<< HEAD
import android.widget.Toast

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
    lateinit var parol_perviy: EditText
    lateinit var parol_povtor: EditText
    lateinit var registr_button: Button
    private lateinit var shared_preferences: SharedPreferences
=======
=======
import android.widget.Toast
>>>>>>> d76c070 (beta 1.0)

class okno_reg : AppCompatActivity() {
    lateinit var registr_name: EditText
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    lateinit var registr_email: EditText
    lateinit var registr_password: EditText
=======
    lateinit var parol_perviy: EditText
    lateinit var parol_povtor: EditText
>>>>>>> f7d9eae (beta 1.0)
    lateinit var registr_button: Button
<<<<<<< HEAD
>>>>>>> abcc495 (Add NSGs work)
=======
    private lateinit var shared_preferences: SharedPreferences
>>>>>>> 66fe6b8 (Fix for last master commit)
=======
=======
    lateinit var registr_email: EditText
>>>>>>> b4b977d (Fix for last master commit)
    lateinit var registr_password: EditText
    lateinit var registr_button: Button
<<<<<<< HEAD
>>>>>>> abcc495 (Add NSGs work)
<<<<<<< HEAD
>>>>>>> 09b46dd (Add NSGs work)
=======
=======
    private lateinit var shared_preferences: SharedPreferences
>>>>>>> 66fe6b8 (Fix for last master commit)
>>>>>>> b4b977d (Fix for last master commit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_reg)

        registr_name = findViewById(R.id.registr_name)
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 09b46dd (Add NSGs work)
=======
>>>>>>> b4b977d (Fix for last master commit)
=======
>>>>>>> f7d9eae (beta 1.0)
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
<<<<<<< HEAD
<<<<<<< HEAD
=======
        registr_email = findViewById(R.id.registr_email)
>>>>>>> 66fe6b8 (Fix for last master commit)
        registr_password = findViewById(R.id.registr_password)
=======
        parol_perviy = findViewById(R.id.parol_perviy)
        parol_povtor = findViewById(R.id.parol_povtor)
>>>>>>> d76c070 (beta 1.0)
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

<<<<<<< HEAD
=======
=======
=======
        registr_email = findViewById(R.id.registr_email)
>>>>>>> 66fe6b8 (Fix for last master commit)
>>>>>>> b4b977d (Fix for last master commit)
        registr_password = findViewById(R.id.registr_password)
        registr_button = findViewById(R.id.registr_button)
        shared_preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        registr_button.setOnClickListener {
<<<<<<< HEAD
>>>>>>> 09b46dd (Add NSGs work)
=======

            val name = registr_name.text.toString().trim()
            val password = registr_password.text.toString().trim()

            val editor = shared_preferences.edit()
            editor.putString("EXTRA_NAME", name)
            editor.putString("EXTRA_PASSWORD", password)
            editor.apply()

>>>>>>> b4b977d (Fix for last master commit)
=======
<<<<<<< HEAD
>>>>>>> f7d9eae (beta 1.0)
            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
>>>>>>> abcc495 (Add NSGs work)
=======
                val intent = Intent(this, glavniy_ekran::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Пароли должны совпадать", Toast.LENGTH_SHORT).show()
            }
>>>>>>> d76c070 (beta 1.0)
        }


    }
}

