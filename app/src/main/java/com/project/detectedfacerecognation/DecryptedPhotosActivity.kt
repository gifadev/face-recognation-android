package com.project.detectedfacerecognation
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DecryptedPhotosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypted_photos)

        // Ambil daftar file foto hasil dekripsi
        val pictureDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val decryptedPhotoFiles = pictureDir?.listFiles { file ->
            file.name.endsWith("_decrypted.jpg") // Filter hanya file hasil dekripsi
        }?.toList() ?: emptyList()

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPhotos)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // Tampilkan 3 kolom
        recyclerView.adapter = DecryptedPhotosAdapter(decryptedPhotoFiles)
    }
}