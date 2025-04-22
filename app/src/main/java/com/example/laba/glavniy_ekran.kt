package com.example.laba

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: ImageButton
    lateinit var cam_button: AppCompatButton
    lateinit var gallery_button: AppCompatButton
    private var currentPhotoPath: String? = null

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

        cam_button.setOnClickListener {
            dispatchTakePictureIntent()
        }

        MobileAds.initialize(this) {}
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this, "com.example.laba.fileprovider", it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
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
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let {
                val photoUri = Uri.fromFile(File(it))
                val intent = Intent(this, ekran_redact::class.java)
                intent.putExtra("imageUri", photoUri.toString())
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 100
        private const val REQUEST_CODE_CAMERA = 200
    }
}