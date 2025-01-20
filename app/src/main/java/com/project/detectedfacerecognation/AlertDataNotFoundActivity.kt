package com.project.detectedfacerecognation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
        val back: ImageView = findViewById(R.id.btn_back)
        val floatingButton: ImageView = findViewById(R.id.floating)

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

        back.setOnClickListener {
            finish()
        }
    }

    private fun startFloatingCamera() {
        val intent = Intent(this, FloatingCameraService::class.java)
        startService(intent)
    }
}