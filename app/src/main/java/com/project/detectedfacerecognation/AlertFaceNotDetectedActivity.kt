package com.project.detectedfacerecognation

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AlertFaceNotDetectedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_face_not_detected)

        val btnAgain = findViewById<ImageView>(R.id.btn_take_again)

        btnAgain.setOnClickListener {
            finish()
        }
    }
}