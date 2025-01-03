package com.project.detectedfacerecognation

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class FloatingCameraService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingIconView: View
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    override fun onCreate() {
        super.onCreate()

        // Periksa izin SYSTEM_ALERT_WINDOW
        if (!Settings.canDrawOverlays(this)) {
            // Jika izin belum diberikan, minta izin
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }

        // Inisialisasi WindowManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate tampilan floating icon
        floatingIconView = LayoutInflater.from(this).inflate(R.layout.floating_camera_layout, null)

        // Konfigurasi WindowManager.LayoutParams untuk floating icon
        val iconParams = WindowManager.LayoutParams(
            70, // Lebar
            70, // Tinggi
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        iconParams.gravity = Gravity.TOP or Gravity.START
        iconParams.x = 0
        iconParams.y = 100

        // Tambahkan tampilan ke WindowManager
        windowManager.addView(floatingIconView, iconParams)

        // Tambahkan listener untuk floating icon
        floatingIconView.findViewById<ImageView>(R.id.floatingIcon).setOnClickListener {
            showMainActivity()
            stopSelf() // Hentikan service untuk menghilangkan floating icon
        }

        // Tambahkan listener untuk drag-and-drop
        floatingIconView.findViewById<ImageView>(R.id.floatingIcon).setOnTouchListener { v, event ->
            val layoutParams = floatingIconView.layoutParams as WindowManager.LayoutParams

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Simpan posisi awal dan posisi sentuh
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    // Hitung perubahan posisi
                    val dx = event.rawX.toInt() - initialTouchX.toInt()
                    val dy = event.rawY.toInt() - initialTouchY.toInt()

                    // Update posisi floating icon
                    layoutParams.x = initialX + dx
                    layoutParams.y = initialY + dy
                    windowManager.updateViewLayout(floatingIconView, layoutParams)
                }
            }
            false
        }
    }

    private fun showMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingIconView.isInitialized) {
            windowManager.removeView(floatingIconView)
        }
    }

    // Implementasi metode onBind
    override fun onBind(intent: Intent?): IBinder? {
        return null // Karena kita tidak menggunakan binding service
    }
}