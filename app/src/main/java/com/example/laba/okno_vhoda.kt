package com.example.laba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
<<<<<<< HEAD
<<<<<<< HEAD
import android.content.SharedPreferences

class okno_vhoda : AppCompatActivity() {
    private lateinit var vhod_name: EditText
    private lateinit var vhod_password: EditText
    private lateinit var vhod_button: Button
    private lateinit var shared_preferences: SharedPreferences
=======

class okno_vhoda : AppCompatActivity() {
    lateinit var vhod_name: EditText
    lateinit var vhod_password: EditText
    lateinit var vhod_button: Button
>>>>>>> abcc495 (Add NSGs work)
=======
import android.content.SharedPreferences

class okno_vhoda : AppCompatActivity() {
    private lateinit var vhod_name: EditText
    private lateinit var vhod_password: EditText
    private lateinit var vhod_button: Button
    private lateinit var shared_preferences: SharedPreferences
>>>>>>> 66fe6b8 (Fix for last master commit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_vhoda)

        vhod_name = findViewById(R.id.vhod_name)
        vhod_password = findViewById(R.id.vhod_password)
        vhod_button = findViewById(R.id.vhod_button)

<<<<<<< HEAD
<<<<<<< HEAD
        shared_preferences = getSharedPreferences("dannie_profilya", MODE_PRIVATE)

        vhod_button.setOnClickListener {
            val name = vhod_name.text.toString()
            val password = vhod_password.text.toString()

            val editor = shared_preferences.edit()
            editor.putString("EXTRA_NAME", name)
            editor.putString("EXTRA_PASSWORD", password)
            editor.apply()

            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }
    }
}
=======
=======
        shared_preferences = getSharedPreferences("dannie_profilya", MODE_PRIVATE)

>>>>>>> 66fe6b8 (Fix for last master commit)
        vhod_button.setOnClickListener {
            val name = vhod_name.text.toString()
            val password = vhod_password.text.toString()

            val editor = shared_preferences.edit()
            editor.putString("EXTRA_NAME", name)
            editor.putString("EXTRA_PASSWORD", password)
            editor.apply()

            val intent = Intent(this, glavniy_ekran::class.java)
            startActivity(intent)
        }
    }
<<<<<<< HEAD

>>>>>>> abcc495 (Add NSGs work)
=======
}
>>>>>>> 66fe6b8 (Fix for last master commit)
