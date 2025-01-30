package com.project.detectedfacerecognation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DecryptedPhotosAdapter(private val photoFiles: List<File>) :
    RecyclerView.Adapter<DecryptedPhotosAdapter.PhotoViewHolder>() {

    // Fungsi ini dipanggil saat RecyclerView membutuhkan ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        // Inflate layout untuk setiap item RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_decrypted_photo, parent, false)
        return PhotoViewHolder(view)
    }

    // Fungsi ini dipanggil untuk mengikat data ke ViewHolder
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // Ambil file foto dari daftar
        val file = photoFiles[position]

        // Decode file menjadi Bitmap
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Set Bitmap ke ImageView
        holder.imageView.setImageBitmap(bitmap)
    }

    // Fungsi ini mengembalikan jumlah item dalam daftar
    override fun getItemCount(): Int {
        return photoFiles.size
    }

    // ViewHolder untuk menyimpan referensi view di setiap item RecyclerView
    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
    }
}