package com.project.detectedfacerecognation

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertDataNotFoundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_data_not_found)

        val btnAgain = findViewById<ImageView>(R.id.btn_take_again)

        btnAgain.setOnClickListener {
            finish()
        }
    }
}