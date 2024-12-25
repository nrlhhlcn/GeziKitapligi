package com.nurullahhilcan.gezikitapligi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.nurullahhilcan.gezikitapligi.databinding.ActivityGeziEkleBinding
import java.io.ByteArrayOutputStream
import java.io.IOException

class GeziEkle : AppCompatActivity() {
    private var selectedBitmap: Bitmap? = null
    private lateinit var binding: ActivityGeziEkleBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val selectedBitmaps = mutableListOf<Bitmap>() // Çoklu resim desteği için
    private lateinit var database: SQLiteDatabase
    private var selectedImageViewId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeziEkleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SQLite veritabanını oluştur
        database = this.openOrCreateDatabase("Trips", Context.MODE_PRIVATE, null)

        // Resim seçme işlemleri için ImageView'ları bağla
        binding.imageView1.setOnClickListener { selectImage(1) }
        binding.imageView2.setOnClickListener { selectImage(2) }
        binding.imageView3.setOnClickListener { selectImage(3) }

        // Launcher'ları kaydet
        registerLauncher()
    }

    fun kaydet(view: View) {
        val tripName = binding.tripName.text.toString()
        val countryName = binding.ulke.text.toString()
        val memory = binding.ani.text.toString()

        if (tripName.isNotEmpty() && countryName.isNotEmpty() && memory.isNotEmpty() && selectedBitmaps.isNotEmpty()) {
            try {
                // Tabloları oluştur
                database.execSQL("CREATE TABLE IF NOT EXISTS trip (trip_id INTEGER PRIMARY KEY, tripName VARCHAR, ulke VARCHAR, ani VARCHAR)")
                database.execSQL("CREATE TABLE IF NOT EXISTS resim (resim_id INTEGER PRIMARY KEY, trip_id INTEGER, image BLOB)")

                // Trip bilgilerini ekle
                val tripStatement = database.compileStatement("INSERT INTO trip (tripName, ulke, ani) VALUES (?, ?, ?)")
                tripStatement.bindString(1, tripName)
                tripStatement.bindString(2, countryName)
                tripStatement.bindString(3, memory)
                val tripId = tripStatement.executeInsert() // Eklenen trip_id'yi al

                // Resimleri ekle
                val resimStatement = database.compileStatement("INSERT INTO resim (trip_id, image) VALUES (?, ?)")
                for (bitmap in selectedBitmaps) {
                    val smallBitmap = makeSmallerBitmap(bitmap, 300)
                    val outputStream = ByteArrayOutputStream()
                    smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                    val byteArray = outputStream.toByteArray()

                    resimStatement.bindLong(1, tripId)
                    resimStatement.bindBlob(2, byteArray)
                    resimStatement.execute()
                }

                // Başarı mesajı ve ana ekrana dön
                Toast.makeText(this, "Gezi başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Lütfen tüm bilgileri doldurun ve en az bir resim seçin.", Toast.LENGTH_LONG).show()
        }
    }

    fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(imageViewId: Int) {
        selectedImageViewId = imageViewId

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Snackbar.make(binding.root, "Galeri izni gerekli", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin Ver") {
                        permissionLauncher.launch(permission)
                    }.show()
            } else {
                permissionLauncher.launch(permission)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(contentResolver, imageData!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                        }

                        when (selectedImageViewId) {
                            1 -> binding.imageView1.setImageBitmap(selectedBitmap)
                            2 -> binding.imageView2.setImageBitmap(selectedBitmap)
                            3 -> binding.imageView3.setImageBitmap(selectedBitmap)
                        }

                        selectedBitmap?.let { selectedBitmaps.add(it) }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(this, "İzin gerekli!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
