package com.example.laba

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: Button
    lateinit var cam_button: FloatingActionButton
    lateinit var gallery_button: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glavniy_ekran)

        pereh_v_profil_button = findViewById(R.id.pereh_v_profil_button)
        cam_button = findViewById(R.id.cam_button)
        gallery_button = findViewById(R.id.gallery_button)

        pereh_v_profil_button.setOnClickListener {
            val intent = Intent(this, profil_menu::class.java)
            startActivity(intent)
        }

        gallery_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                val intent = Intent(this, ekran_redact::class.java)
                intent.putExtra("imageUri", it.toString())
                startActivity(intent)
            }
        }
    }


    companion object {
        private const val REQUEST_CODE_GALLERY = 100
    }

}