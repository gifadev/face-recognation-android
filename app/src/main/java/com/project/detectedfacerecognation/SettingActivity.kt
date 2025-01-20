package com.project.detectedfacerecognation
import android.os.Bundle
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val back : ImageView = findViewById(R.id.btn_back)
        val version1Button: Button = findViewById(R.id.btn_version1)
        val version2Button: Button = findViewById(R.id.btn_version2)
        val floatingButton: ImageView = findViewById(R.id.floating)
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()


        version1Button.setOnClickListener {
            editor.putString("API_VERSION", "v1")
            editor.apply()
            Toast.makeText(this, "version 1 is selected", Toast.LENGTH_SHORT).show()
        }

        version2Button.setOnClickListener {
            editor.putString("API_VERSION", "v2")
            editor.apply()
            Toast.makeText(this, "version 2 is selected", Toast.LENGTH_SHORT).show()
        }
        back.setOnClickListener {
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

    private fun startFloatingCamera() {
        val intent = Intent(this, FloatingCameraService::class.java)
        startService(intent)
    }
}