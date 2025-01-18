package com.project.detectedfacerecognation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.project.detectedfacerecognation.api.ApiRest
import com.project.detectedfacerecognation.api.UtilsApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import android.provider.Settings
import android.util.Base64
import android.view.Surface
import android.widget.TextView
import android.os.Handler
import android.os.Looper


class MainActivity : AppCompatActivity() {
    var GET_FROM_GALLERY: Int = 141
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private lateinit var loading: RelativeLayout
    private lateinit var faceNotFound: RelativeLayout
    private var mPhotoUri: Uri? = null
    lateinit var context: Context
    var filePhoto : File? = null
    var taken = false
    private lateinit var tvStatus: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            // Handle permission denial
        }
    }

    private lateinit var previewView: androidx.camera.view.PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        previewView = findViewById(R.id.previewView)
        freezeFrame = findViewById(R.id.freezeFrame)
        val floatingButton: ImageView = findViewById(R.id.floating)
        val toggleButton: ImageView = findViewById(R.id.toggleCameraButton)
        val captureButton: ImageView = findViewById(R.id.captureButton)
        val uploadButton: ImageView = findViewById(R.id.uploadButton)
        val settingButton: ImageView = findViewById(R.id.setting)
        faceNotFound = findViewById(R.id.not_found)
        loading = findViewById(R.id.loading)
        tvStatus = findViewById(R.id.tvStatus)

        context = this@MainActivity

        setButtonEffect(toggleButton)
        setButtonEffect(captureButton)
        setButtonEffect(uploadButton)

        // Tambahkan listener untuk tombol
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
                finish()
            }
        }

        settingButton.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        // Check for permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        toggleButton.setOnClickListener {
            toggleCamera()
        }

        // Capture photo when capture button is clicked
        captureButton.setOnClickListener {
            if (!taken) {
                takePhoto()  // Ambil foto
                taken = true  // Tandai foto sudah diambil
            } else {
                faceNotFound.visibility = View.GONE
                startCamera()  // Restart kamera jika sudah diambil
                taken = false  // Reset flag untuk pengambilan foto berikutnya
            }
        }

        uploadButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    ), GET_FROM_GALLERY
                )
            } else {
                requestPermissions()
            }
        }

        faceNotFound.setOnClickListener {
            faceNotFound.visibility = View.GONE // Sembunyikan notifikasi
            startCamera() // Mulai ulang kamera
            taken = false // Reset status pengambilan foto
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(android.Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        requestPermissionLauncher.launch(permissions.toTypedArray().toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingCamera()
            } else {
                Toast.makeText(this, "Izin SYSTEM_ALERT_WINDOW diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK) {
            val selectedImage = data?.data
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                mPhotoUri = getImageUri(context, bitmap)

//                JIKA API SUDAH ADA
                sendDataFromGalery()

            } catch (e: FileNotFoundException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonEffect(view: View) {
        view.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Kurangi alpha untuk memberikan efek redup
                    view.alpha = 0.5f
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    // Kembalikan alpha ke nilai semula
                    view.alpha = 1.0f
                }
            }
            false
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Set up image capture use case
            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the camera to lifecycle, along with preview and image capture use cases
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startFloatingCamera() {
        val intent = Intent(this, FloatingCameraService::class.java)
        startService(intent)
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()  // Restart camera with the new camera selector
    }

    private lateinit var freezeFrame: ImageView

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return


        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        filePhoto = photoFile

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraX", "Photo saved at ${photoFile.absolutePath}")
                    processImage(photoFile)

                    taken = false
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)

                    taken = false
                }
            }
        )
    }


    private fun processImage(photoFile: File) {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

        // Resize the image if it's too large
        val resizedBitmap = resizeImage(bitmap, 1024) // Resize to a max width of 1024px

        val faceDetector: FaceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
        sendData()
    }

    private fun extracted() {
        filePhoto
    }

    private fun resizeImage(image: Bitmap, maxWidth: Int): Bitmap {
        val aspectRatio = image.width.toFloat() / image.height.toFloat()
        val newWidth = if (image.width > maxWidth) maxWidth else image.width
        val newHeight = (newWidth / aspectRatio).toInt()

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true)
    }

    private fun saveCroppedImage(croppedBitmap: Bitmap, file: File) {
        val outStream = FileOutputStream(file)

        // Compress the image to a lower quality to reduce size
        val quality = 80 // Adjust the quality to reduce size
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
        outStream.flush()
        outStream.close()

        // Check if the file size is over 1MB, and if so, reduce quality further
        if (file.length() > 1 * 1024 * 1024) {
            reduceImageSize(file)
        }
    }

    private fun reduceImageSize(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val outStream = FileOutputStream(file)

        // Reduce quality further if file size is over 1MB
        val quality = 70
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
        outStream.flush()
        outStream.close()
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val imageName = randomAlphaNumeric(20)
        val path = MediaStore.Images.Media.insertImage(
            inContext!!.contentResolver,
            inImage,
            imageName,
            null
        )
        return Uri.parse(path)
    }

    fun randomAlphaNumeric(count: Int): String {
        var count = count
        val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz0123456789"
        val builder = StringBuilder()
        while (count-- != 0) {
            val character = (Math.random() * ALPHA_NUMERIC_STRING.length).toInt()
            builder.append(ALPHA_NUMERIC_STRING[character])
        }
        return builder.toString()
    }

    private fun getRealPathFromURIPath(contentURI: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
        return if (cursor == null) {
            contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

    // Fungsi untuk mengonversi base64 ke Bitmap
    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Pastikan string memiliki panjang 8 karakter (YYYYMMDD)
            if (dateString.length == 8) {
                val year = dateString.substring(0, 4)
                val month = dateString.substring(4, 6)
                val day = dateString.substring(6, 8)
                "$year-$month-$day" // Format menjadi YYYY-MM-DD
            } else {
                dateString // Jika format tidak sesuai, kembalikan string asli
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // Jika terjadi error, kembalikan string asli
        }
    }

    private fun formatGender(genderCode: String): String {
        return when (genderCode) {
            "M" -> "Male"
            "F" -> "Female"
            else -> genderCode // Jika tidak sesuai, kembalikan nilai asli
        }
    }

    private fun sendData() {
        loading.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Searching..."
        // menampilkan kamera freeze
        previewView.bitmap?.let {
            freezeFrame.setImageBitmap(it)
            freezeFrame.visibility = View.VISIBLE
        }
        var image: MultipartBody.Part? = null
        var mFile: RequestBody? = null

        // Pastikan filePhoto tidak null
        if (filePhoto == null) {
            Toast.makeText(context, "Silahkan Upload Foto", Toast.LENGTH_SHORT).show()
            loading.visibility = View.GONE
            // Hilangkan  freeze
            freezeFrame.visibility = View.GONE
            return
        }

        // Inisialisasi RequestBody dari filePhoto
        mFile = RequestBody.create("image/*".toMediaTypeOrNull(), filePhoto!!)

        // Inisialisasi ApiRest dan SharedPreferences
        val mApiRest: ApiRest = UtilsApi.getAPIService(this) as ApiRest
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Cek versi API yang digunakan
        if (sharedPref.getString("API_VERSION", "v2") == "v1") {
            image = MultipartBody.Part.createFormData("image", filePhoto!!.name, mFile)
            mApiRest.sendPictureV1(image)?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonResponse = JSONObject(response.body().toString())
                            val status = jsonResponse.getString("status")
                            val message = jsonResponse.getString("message")

                            when (status) {

                                "success" -> {
                                    val data = jsonResponse.optJSONObject("data")
                                    if (data != null) {
                                        val intent = when (data.optString("status")) {
                                            else -> Intent(context, ResultActivity::class.java).apply {
                                                putExtra("image_url", data.getString("image_url"))
                                                putExtra("full_name", data.getString("full_name"))
                                                putExtra("birth_place", data.getString("birth_place"))
                                                putExtra("birth_date", data.getString("birth_date"))
                                                putExtra("address", data.getString("address"))
                                                putExtra("nationality", data.getString("nationality"))
                                                putExtra("passport_number", data.getString("passport_number"))
                                                putExtra("gender", data.getString("gender"))
                                                putExtra("national_id_number", data.getString("national_id_number"))
                                                putExtra("marital_status", data.getString("marital_status"))
                                                putExtra("score", data.getString("score"))
                                            }
                                        }
                                        tvStatus.visibility = View.VISIBLE
                                        tvStatus.text = "Identifying..."
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            // Menunggu selama 2 detik sebelum memulai Activity
                                            startActivity(intent)
                                            loading.visibility = View.GONE
                                            tvStatus.visibility = View.GONE
                                            freezeFrame.visibility = View.GONE
                                        }, 2000)
                                    }
                                }

                                else -> {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(context, "Terjadi kesalahan dalam memproses data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        // Handle non-successful responses (e.g., 400 Bad Request)
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrEmpty()) {
                                val jsonError = JSONObject(errorBody)
                                val status = jsonError.getString("status")
                                val message = jsonError.getString("message")

                                when (status) {
                                    "error" -> {
                                        when (message) {
                                            "No faces detected in the image" -> {
                                                Log.d("API_RESPONSE", "No faces detected, redirecting to AlertFaceNotDetectedActivity")
                                                val intent = Intent(context, AlertFaceNotDetectedActivity::class.java)
                                                tvStatus.visibility = View.VISIBLE
                                                tvStatus.text = "Identifying..."
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    // Menunggu selama 2 detik sebelum memulai Activity
                                                    loading.visibility = View.GONE
                                                    tvStatus.visibility = View.GONE
                                                    freezeFrame.visibility = View.GONE
                                                    startActivity(intent)
                                                }, 2000)
                                            }
                                            "No matching identity found" -> {
                                                Log.d("API_RESPONSE", "No matching identity found, redirecting to AlertDataNotFoundActivity")
                                                val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                                tvStatus.visibility = View.VISIBLE
                                                tvStatus.text = "Identifying..."
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    // Menunggu selama 2 detik sebelum memulai Activity
                                                    loading.visibility = View.GONE
                                                    tvStatus.visibility = View.GONE
                                                    freezeFrame.visibility = View.GONE
                                                    startActivity(intent)
                                                }, 2000)
                                            }
                                            else -> {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    else -> {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Gagal Upload Data", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Terjadi kesalahan dalam memproses data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    loading.visibility = View.GONE
                    Log.e("API_ERROR", "Koneksi Error: ${t.message ?: "Unknown error"}")
                    Toast.makeText(context, "Koneksi Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            // API V2: Gunakan field "file"
            image = MultipartBody.Part.createFormData("file", filePhoto!!.name, mFile)
            mApiRest.sendPictureV2(image)?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonResponse = JSONObject(response.body().toString())
                            Log.d("API_RESPONSE", "Full Response: $jsonResponse") // Cetak respons lengkap

                            // Pastikan field "status" ada
                            if (jsonResponse.has("status")) {
                                val status = jsonResponse.getString("status")
                                Log.d("API_RESPONSE", "Status: $status")

                                when (status) {
                                    "success" -> {
                                        // Pastikan field "response" ada
                                        if (jsonResponse.has("response")) {
                                            val responseObj = jsonResponse.getJSONObject("response")

                                            // Pastikan field "results" ada
                                            if (responseObj.has("results")) {
                                                val resultsArray = responseObj.getJSONArray("results")
                                                Log.d("API_RESPONSE", "Results Array: $resultsArray")

                                                if (resultsArray.length() > 0) {
                                                    val firstResult = resultsArray.getJSONObject(0)

                                                    // Pastikan field "demographics" ada
                                                    if (firstResult.has("demographics")) {
                                                        val demographics = firstResult.getJSONObject("demographics")
                                                        Log.d("API_RESPONSE", "Demographics: $demographics")

                                                        // Ekstrak data dari objek demographics
                                                        val fullName = demographics.optString("fname", "N/A")
                                                        val gender = formatGender(demographics.optString("gender", "N/A"))
                                                        val birthDate = formatDate(demographics.optString("dob", "N/A"))
                                                        val nationality = demographics.optString("nat", "N/A")
                                                        val passportNumber = demographics.optString("nopaspor", "N/A")

                                                        // Ekstrak data facial (gambar dan skor)
                                                        val facial = firstResult.getJSONObject("facial")
                                                        val score = facial.optInt("score", 0)
                                                        val base64Image = facial.optString("img", "")

                                                        // Konversi base64 ke Bitmap
                                                        val bitmap = base64ToBitmap(base64Image)

                                                        // Buat intent untuk membuka ResultActivity
                                                        val intent = Intent(context, ResultActivity::class.java).apply {
                                                            putExtra("full_name", fullName)
                                                            putExtra("gender", gender)
                                                            putExtra("birth_date", birthDate)
                                                            putExtra("nationality", nationality)
                                                            putExtra("passport_number", passportNumber)
                                                            putExtra("score", score) // Tambahkan skor ke intent
                                                            if (bitmap != null) {
                                                                // Simpan bitmap sebagai byte array
                                                                val stream = ByteArrayOutputStream()
                                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                                                val byteArray = stream.toByteArray()
                                                                putExtra("image_bitmap", byteArray)
                                                            }
                                                        }
                                                        startActivity(intent)
                                                    } else {
                                                        Toast.makeText(context, "Field 'demographics' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Log.d("API_RESPONSE", "Data tidak ditemukan, redirecting to AlertDataNotFoundActivity")
                                                    val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                                    startActivity(intent)
                                                }
                                            } else {
                                                Toast.makeText(context, "Field 'results' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Field 'response' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    "error" -> {
                                        val message = jsonResponse.optString("message", "Terjadi kesalahan yang tidak diketahui.")
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Log.d("API_RESPONSE", "Status tidak dikenali, redirecting to AlertDataNotFoundActivity")
                                        val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                        tvStatus.visibility = View.VISIBLE
                                        tvStatus.text = "Identifying..."
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            // Menunggu selama 2 detik sebelum memulai Activity
                                            loading.visibility = View.GONE
                                            tvStatus.visibility = View.GONE
                                            freezeFrame.visibility = View.GONE
                                            startActivity(intent)
                                        }, 2000)
//
                                    }
                                }
                            } else {
                                Log.d("API_RESPONSE", "No faces detected, redirecting to AlertFaceNotDetectedActivity")
                                val intent = Intent(context, AlertFaceNotDetectedActivity::class.java)
                                tvStatus.visibility = View.VISIBLE
                                tvStatus.text = "Identifying..."
                                Handler(Looper.getMainLooper()).postDelayed({
                                    // Menunggu selama 2 detik sebelum memulai Activity
                                    loading.visibility = View.GONE
                                    tvStatus.visibility = View.GONE
                                    freezeFrame.visibility = View.GONE
                                    startActivity(intent)
                                }, 2000)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.e("API_RESPONSE", "Error parsing JSON: ${e.message}")
                            Toast.makeText(context, "Error parsing response.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_RESPONSE", "Error: ${response.code()}, $errorBody")
                        Toast.makeText(context, "Terjadi kesalahan: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    loading.visibility = View.GONE
                    Log.e("API_ERROR", "Koneksi Error: ${t.message ?: "Unknown error"}", t)
                    Toast.makeText(context, "Koneksi Error: ${t.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun sendDataFromGalery() {
        loading.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Searching..."
        // menampilkan kamera freeze
        previewView.bitmap?.let {
            freezeFrame.setImageBitmap(it)
            freezeFrame.visibility = View.VISIBLE
        }
        var image: MultipartBody.Part? = null
        var file: File? = null
        var mFile: RequestBody? = null

        if (mPhotoUri != null) {
            val filePath = getRealPathFromURIPath(mPhotoUri!!, this)
            file = File(filePath)
            mFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        }

        // Pastikan file dan mFile tidak null sebelum digunakan
        if (file == null || mFile == null) {
            Toast.makeText(context, "File tidak valid.", Toast.LENGTH_SHORT).show()
            loading.visibility = View.GONE
            return
        }

        val mApiRest: ApiRest = UtilsApi.getAPIService(this) as ApiRest
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        if (sharedPref.getString("API_VERSION", "v2") == "v1") {
            // API V1: Gunakan field "image"
            image = MultipartBody.Part.createFormData("image", file.name, mFile)
            mApiRest.sendPictureV1(image)?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonResponse = JSONObject(response.body().toString())
                            val status = jsonResponse.getString("status")
                            val message = jsonResponse.getString("message")

                            when (status) {
                                "success" -> {
                                    val data = jsonResponse.optJSONObject("data")
                                    if (data != null) {
                                        val intent = when (data.optString("status")) {
                                            "No faces detected in the image" -> Intent(context, AlertFaceNotDetectedActivity::class.java)
                                            else -> Intent(context, ResultActivity::class.java).apply {
                                                putExtra("image_url", data.getString("image_url"))
                                                putExtra("full_name", data.getString("full_name"))
                                                putExtra("birth_place", data.getString("birth_place"))
                                                putExtra("birth_date", data.getString("birth_date"))
                                                putExtra("address", data.getString("address"))
                                                putExtra("nationality", data.getString("nationality"))
                                                putExtra("passport_number", data.getString("passport_number"))
                                                putExtra("gender", data.getString("gender"))
                                                putExtra("national_id_number", data.getString("national_id_number"))
                                                putExtra("marital_status", data.getString("marital_status"))
                                                putExtra("score", data.getString("score"))
                                            }
                                        }
                                        tvStatus.visibility = View.VISIBLE
                                        tvStatus.text = "Identifying..."
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            // Menunggu selama 2 detik sebelum memulai Activity
                                            startActivity(intent)
                                            loading.visibility = View.GONE
                                            tvStatus.visibility = View.GONE
                                            freezeFrame.visibility = View.GONE
                                        }, 2000)
                                    }
                                }

                                else -> {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(context, "Terjadi kesalahan dalam memproses data.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Handle non-successful responses (e.g., 400 Bad Request)
                        try {
                            val errorBody = response.errorBody()?.string()
                            if (!errorBody.isNullOrEmpty()) {
                                val jsonError = JSONObject(errorBody)
                                val status = jsonError.getString("status")
                                val message = jsonError.getString("message")

                                when (status) {
                                    "error" -> {
                                        when (message) {
                                            "No faces detected in the image" -> {
                                                Log.d("API_RESPONSE", "No faces detected, redirecting to AlertFaceNotDetectedActivity")
                                                val intent = Intent(context, AlertFaceNotDetectedActivity::class.java)
                                                tvStatus.visibility = View.VISIBLE
                                                tvStatus.text = "Identifying..."
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    // Menunggu selama 2 detik sebelum memulai Activity
                                                    startActivity(intent)
                                                    loading.visibility = View.GONE
                                                    tvStatus.visibility = View.GONE
                                                    freezeFrame.visibility = View.GONE
                                                }, 2000)
                                            }
                                            "No matching identity found" -> {
                                                Log.d("API_RESPONSE", "No matching identity found, redirecting to AlertDataNotFoundActivity")
                                                val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                                tvStatus.visibility = View.VISIBLE
                                                tvStatus.text = "Identifying..."
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    // Menunggu selama 2 detik sebelum memulai Activity
                                                    startActivity(intent)
                                                    loading.visibility = View.GONE
                                                    tvStatus.visibility = View.GONE
                                                    freezeFrame.visibility = View.GONE
                                                }, 2000)
                                            }
                                            else -> {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    else -> {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Gagal Upload Data", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Terjadi kesalahan dalam memproses data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    loading.visibility = View.GONE
                    Log.e("API_ERROR", "Koneksi Error: ${t.message ?: "Unknown error"}")
                    Toast.makeText(context, "Koneksi Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            // API V2: Gunakan field "file"
            image = MultipartBody.Part.createFormData("file", filePhoto!!.name, mFile)
            mApiRest.sendPictureV2(image)?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonResponse = JSONObject(response.body().toString())
                            Log.d("API_RESPONSE", "Full Response: $jsonResponse") // Cetak respons lengkap

                            // Pastikan field "status" ada
                            if (jsonResponse.has("status")) {
                                val status = jsonResponse.getString("status")
                                Log.d("API_RESPONSE", "Status: $status")

                                when (status) {
                                    "success" -> {
                                        // Pastikan field "response" ada
                                        if (jsonResponse.has("response")) {
                                            val responseObj = jsonResponse.getJSONObject("response")

                                            // Pastikan field "results" ada
                                            if (responseObj.has("results")) {
                                                val resultsArray = responseObj.getJSONArray("results")
                                                Log.d("API_RESPONSE", "Results Array: $resultsArray")

                                                if (resultsArray.length() > 0) {
                                                    val firstResult = resultsArray.getJSONObject(0)

                                                    // Pastikan field "demographics" ada
                                                    if (firstResult.has("demographics")) {
                                                        val demographics = firstResult.getJSONObject("demographics")
                                                        Log.d("API_RESPONSE", "Demographics: $demographics")

                                                        // Ekstrak data dari objek demographics
                                                        val fullName = demographics.optString("fname", "N/A")
                                                        val gender = formatGender(demographics.optString("gender", "N/A"))
                                                        val birthDate = formatDate(demographics.optString("dob", "N/A"))
                                                        val nationality = demographics.optString("nat", "N/A")
                                                        val passportNumber = demographics.optString("nopaspor", "N/A")

                                                        // Ekstrak data facial (gambar dan skor)
                                                        val facial = firstResult.getJSONObject("facial")
                                                        val score = facial.optInt("score", 0)
                                                        val base64Image = facial.optString("img", "")

                                                        // Konversi base64 ke Bitmap
                                                        val bitmap = base64ToBitmap(base64Image)

                                                        // Buat intent untuk membuka ResultActivity
                                                        val intent = Intent(context, ResultActivity::class.java).apply {
                                                            putExtra("full_name", fullName)
                                                            putExtra("gender", gender)
                                                            putExtra("birth_date", birthDate)
                                                            putExtra("nationality", nationality)
                                                            putExtra("passport_number", passportNumber)
                                                            putExtra("score", score) // Tambahkan skor ke intent
                                                            if (bitmap != null) {
                                                                // Simpan bitmap sebagai byte array
                                                                val stream = ByteArrayOutputStream()
                                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                                                val byteArray = stream.toByteArray()
                                                                putExtra("image_bitmap", byteArray)
                                                            }
                                                        }
                                                        startActivity(intent)
                                                    } else {
                                                        Toast.makeText(context, "Field 'demographics' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Log.d("API_RESPONSE", "Data tidak ditemukan, redirecting to AlertDataNotFoundActivity")
                                                    val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                                    startActivity(intent)
                                                }
                                            } else {
                                                Toast.makeText(context, "Field 'results' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Field 'response' tidak ditemukan.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    "error" -> {
                                        val message = jsonResponse.optString("message", "Terjadi kesalahan yang tidak diketahui.")
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Log.d("API_RESPONSE", "Status tidak dikenali, redirecting to AlertDataNotFoundActivity")
                                        val intent = Intent(context, AlertDataNotFoundActivity::class.java)
                                        tvStatus.visibility = View.VISIBLE
                                        tvStatus.text = "Identifying..."
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            // Menunggu selama 2 detik sebelum memulai Activity
                                            loading.visibility = View.GONE
                                            tvStatus.visibility = View.GONE
                                            freezeFrame.visibility = View.GONE
                                            startActivity(intent)
                                        }, 2000)
//
                                    }
                                }
                            } else {
                                Log.d("API_RESPONSE", "No faces detected, redirecting to AlertFaceNotDetectedActivity")
                                val intent = Intent(context, AlertFaceNotDetectedActivity::class.java)
                                tvStatus.visibility = View.VISIBLE
                                tvStatus.text = "Identifying..."
                                Handler(Looper.getMainLooper()).postDelayed({
                                    // Menunggu selama 2 detik sebelum memulai Activity
                                    loading.visibility = View.GONE
                                    tvStatus.visibility = View.GONE
                                    freezeFrame.visibility = View.GONE
                                    startActivity(intent)
                                }, 2000)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.e("API_RESPONSE", "Error parsing JSON: ${e.message}")
                            Toast.makeText(context, "Error parsing response.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_RESPONSE", "Error: ${response.code()}, $errorBody")
                        Toast.makeText(context, "Terjadi kesalahan: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    loading.visibility = View.GONE
                    Log.e("API_ERROR", "Koneksi Error: ${t.message ?: "Unknown error"}", t)
                    Toast.makeText(context, "Koneksi Error: ${t.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}