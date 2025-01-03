package com.project.detectedfacerecognation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop


class ResultActivity : AppCompatActivity() {

    lateinit var context : Context

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        context = this@ResultActivity

        val back : ImageView = findViewById(R.id.btn_back)

        val imgvFace: ImageView = findViewById(R.id.imgv_face)
        val tvName: TextView = findViewById(R.id.tv_name)
        val tvPlace: TextView = findViewById(R.id.tv_place)
        val tvAddress: TextView = findViewById(R.id.tv_address)
        val tvNationality: TextView = findViewById(R.id.tv_nationality)
        val tvPassport: TextView = findViewById(R.id.tv_passport)
        val tvGender: TextView = findViewById(R.id.tv_gender)
        val tvNationalid: TextView = findViewById(R.id.tv_nationalid)
        val tvMarital: TextView = findViewById(R.id.tv_marital)
        val tvScore: TextView = findViewById(R.id.tv_score)

        // Get data from the intent
        val imgFace = intent.getStringExtra("image_url")
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

        // Set data to views
        tvName.text = name ?: "Unknown"
        tvPlace.text = "${place ?: "Unknown"}, ${date ?: "Unknown"}"
        tvAddress.text = address ?: "Unknown"
        tvNationality.text = nationality ?: "Unknown"
        tvPassport.text = passport ?: "Unknown"
        tvGender.text = gender ?: "Unknown"
        tvNationalid.text = nationalid ?: "Unknown"
        tvMarital.text = marital ?: "Unknown"
        tvScore.text = score ?: "N/A"

//        tvName.text = name ?: "Unknown"
//        tvPlace.text = "Place/Date: ${place ?: "Unknown"}, ${date ?: "Unknown"}"
//        tvAddress.text = "Address: ${address ?: "Unknown"}"
//        tvNationality.text = "Nationality: ${nationality ?: "Unknown"}"
//        tvPassport.text = "Passport Number: ${passport ?: "Unknown"}"
//        tvGender.text = "Gender: ${gender ?: "Unknown"}"
//        tvNationalid.text = "National ID: ${nationalid ?: "Unknown"}"
//        tvMarital.text = "Marital Status: ${marital ?: "Unknown"}"
//        tvScore.text = "Score: ${score ?: "N/A"}"

        // Load images using Glide
        if (!imgFace.isNullOrEmpty()) {
            Glide.with(this)
                .load(imgFace)
                .placeholder(R.drawable.ic_launcher_background)
                .transform(CircleCrop()) // This will make the image round
                .into(imgvFace)
        }

        back.setOnClickListener {
            finish()
        }
    }
}