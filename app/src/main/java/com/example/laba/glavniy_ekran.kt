package com.example.laba

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
=======
>>>>>>> ceba9e1 (beta 1.0)
>>>>>>> 7745933 (beta 1.0)
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
<<<<<<< HEAD
import com.google.android.gms.ads.MobileAds
<<<<<<< HEAD
=======
>>>>>>> ceba9e1 (beta 1.0)
import com.google.android.material.floatingactionbutton.FloatingActionButton

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: Button
    lateinit var cam_button: FloatingActionButton
    lateinit var gallery_button: FloatingActionButton

<<<<<<< HEAD
=======
<<<<<<< HEAD
<<<<<<< HEAD
=======
import android.app.Activity
>>>>>>> 66fe6b8 (Fix for last master commit)
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
=======
>>>>>>> d76c070 (beta 1.0)
import com.google.android.material.floatingactionbutton.FloatingActionButton

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: Button
<<<<<<< HEAD
>>>>>>> abcc495 (Add NSGs work)
=======
    lateinit var cam_button: FloatingActionButton
    lateinit var gallery_button: FloatingActionButton

>>>>>>> 66fe6b8 (Fix for last master commit)
=======
<<<<<<< HEAD
=======
=======
import android.app.Activity
>>>>>>> 66fe6b8 (Fix for last master commit)
>>>>>>> b4b977d (Fix for last master commit)
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class glavniy_ekran : AppCompatActivity() {
    lateinit var pereh_v_profil_button: Button
<<<<<<< HEAD
>>>>>>> abcc495 (Add NSGs work)
<<<<<<< HEAD
>>>>>>> 09b46dd (Add NSGs work)
=======
=======
    lateinit var cam_button: FloatingActionButton
    lateinit var gallery_button: FloatingActionButton

>>>>>>> 66fe6b8 (Fix for last master commit)
>>>>>>> b4b977d (Fix for last master commit)
=======
>>>>>>> ceba9e1 (beta 1.0)
>>>>>>> 7745933 (beta 1.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glavniy_ekran)

        pereh_v_profil_button = findViewById(R.id.pereh_v_profil_button)
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
>>>>>>> 7745933 (beta 1.0)
        cam_button = findViewById(R.id.cam_button)
        gallery_button = findViewById(R.id.gallery_button)
=======
>>>>>>> abcc495 (Add NSGs work)
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> b4b977d (Fix for last master commit)
=======
        cam_button = findViewById(R.id.cam_button)
        gallery_button = findViewById(R.id.gallery_button)
>>>>>>> 66fe6b8 (Fix for last master commit)
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 09b46dd (Add NSGs work)
=======
>>>>>>> b4b977d (Fix for last master commit)
=======
=======
        cam_button = findViewById(R.id.cam_button)
        gallery_button = findViewById(R.id.gallery_button)
>>>>>>> ceba9e1 (beta 1.0)
>>>>>>> 7745933 (beta 1.0)

        pereh_v_profil_button.setOnClickListener {
            val intent = Intent(this, profil_menu::class.java)
            startActivity(intent)
        }
<<<<<<< HEAD
        //tezt

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 66fe6b8 (Fix for last master commit)
=======
>>>>>>> ceba9e1 (beta 1.0)
        gallery_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        MobileAds.initialize(this) {}
=======

>>>>>>> ceba9e1 (beta 1.0)
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

<<<<<<< HEAD
}
=======
<<<<<<< HEAD

        gallery_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        }

    }
<<<<<<< HEAD
}
>>>>>>> abcc495 (Add NSGs work)
=======

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
>>>>>>> 66fe6b8 (Fix for last master commit)
=======
    }
}
>>>>>>> abcc495 (Add NSGs work)
<<<<<<< HEAD
>>>>>>> 09b46dd (Add NSGs work)
=======
=======

=======
        MobileAds.initialize(this) {}
>>>>>>> d76c070 (beta 1.0)
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
>>>>>>> 66fe6b8 (Fix for last master commit)
<<<<<<< HEAD
>>>>>>> b4b977d (Fix for last master commit)
=======
=======
}
>>>>>>> ceba9e1 (beta 1.0)
>>>>>>> 7745933 (beta 1.0)
