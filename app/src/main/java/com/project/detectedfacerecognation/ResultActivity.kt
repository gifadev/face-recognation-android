package com.project.detectedfacerecognation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop


class ResultActivity : AppCompatActivity() {

    lateinit var context: Context

    @SuppressLint("MissingInflatedId")
    private fun startFloatingCamera() {
        val intent = Intent(this, FloatingCameraService::class.java)
        startService(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        context = this@ResultActivity

        val floatingButton: ImageView = findViewById(R.id.floating)
        // Inisialisasi komponen UI
        val back: ImageView = findViewById(R.id.btn_back)
        val imgvFace: ImageView = findViewById(R.id.imgv_face)
        val tvName: TextView = findViewById(R.id.tv_name)
        val tvPlace: TextView = findViewById(R.id.tv_place)
        val tvDate: TextView = findViewById(R.id.tv_date)
        val tvAddress: TextView = findViewById(R.id.tv_address)
        val tvNationality: TextView = findViewById(R.id.tv_nationality)
        val tvPassport: TextView = findViewById(R.id.tv_passport)
        val tvGender: TextView = findViewById(R.id.tv_gender)
        val tvNationalid: TextView = findViewById(R.id.tv_nationalid)
        val tvMarital: TextView = findViewById(R.id.tv_marital)
        val tvScore: TextView = findViewById(R.id.tv_score)

        // Ambil data dari intent
        val imgFace = intent.getStringExtra("image_url") // Untuk API V1
        val byteArray = intent.getByteArrayExtra("image_bitmap") // Untuk API V2
        val name = intent.getStringExtra("full_name")
        val place = intent.getStringExtra("birth_place")
        val date = intent.getStringExtra("birth_date")
        val address = intent.getStringExtra("address")
        val nationality = intent.getStringExtra("nationality")
        val passport = intent.getStringExtra("passport_number")
        val gender = intent.getStringExtra("gender")
        val nationalid = intent.getStringExtra("national_id_number")
        val marital = intent.getStringExtra("marital_status")
        val score = intent.getStringExtra("score")

        // Set data ke TextView
        tvName.text = name ?: "Unknown"
        tvPlace.text = place ?: "Unknown"
        tvDate.text = date ?: "Unknown"
        tvAddress.text = address ?: "Unknown"
        tvNationality.text = nationality ?: "Unknown"
        tvPassport.text = passport ?: "Unknown"
        tvGender.text = gender ?: "Unknown"
        tvNationalid.text = nationalid ?: "Unknown"
        tvMarital.text = marital ?: "Unknown"
        tvScore.text = score ?: "N/A"

        // Tampilkan gambar
        if (!imgFace.isNullOrEmpty()) {
            // Jika gambar berasal dari URL (API V1)
            Glide.with(this)
                .load(imgFace)
                .placeholder(R.drawable.ic_launcher_background)
                .transform(CircleCrop()) // Ini akan membuat gambar bulat
                .into(imgvFace)
        } else if (byteArray != null) {
            // Jika gambar berasal dari base64 (API V2)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imgvFace.setImageBitmap(bitmap)
        } else {
            // Jika tidak ada gambar
            Toast.makeText(this, "Gambar tidak tersedia", Toast.LENGTH_SHORT).show()
        }

        // Kembali ke activity sebelumnya
        back.setOnClickListener {
            finish()
        }

        val btnAgain = findViewById<ImageView>(R.id.btn_take_again)

        btnAgain.setOnClickListener {
            finish()
        }


        floatingButton.setOnClickListener {
            // Minta izin SYSTEM_ALERT_WINDOW jika belum diberikan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 100)
            } else {
                // Jika izin sudah diberikan, mulai FloatingCameraService
                startFloatingCamera()
                finishAffinity()
            }
        }
    }
}