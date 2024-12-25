package com.nurullahhilcan.gezikitapligi

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
        database = this.openOrCreateDatabase("Geziler", Context.MODE_PRIVATE, null)
        createDatabaseTables()

        // Resim seçme işlemleri için ImageView'ları bağla
        binding.imageView1.setOnClickListener { selectImage(1) }
        binding.imageView2.setOnClickListener { selectImage(2) }
        binding.imageView3.setOnClickListener { selectImage(3) }

        // Launcher'ları kaydet
        registerLauncher()




        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Seçilen öğeyi al
                val selectedCategory = parent.getItemAtPosition(position).toString()

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Boş seçim durumu
            }
        }
    }

    private fun createDatabaseTables() {
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS trip (trip_id INTEGER PRIMARY KEY, user_id VARCHAR, tripName VARCHAR, ulke VARCHAR, ani VARCHAR)")
            database.execSQL("CREATE TABLE IF NOT EXISTS resimler (resim_id INTEGER PRIMARY KEY,user_id VARCHAR, trip_id INTEGER, image BLOB)")
            Log.d("GeziEkle", "Tablolar başarıyla oluşturuldu.")
        } catch (e: Exception) {
            Log.e("GeziEkle", "Tablo oluşturulurken hata: ${e.localizedMessage}")
        }
    }
    fun openDatePicker(view: View) {
        // Tarih seçici açmak için bir DatePickerDialog kullanıyoruz
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Seçilen tarihi bir String olarak alıyoruz
                val date = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
              binding.saatText.text= date  // Seçilen tarihi TextView'da gösteriyoruz
            },
            year, month, day
        )

        datePickerDialog.show()  // DatePicker'ı gösteriyoruz
    }

    fun kaydet(view: View) {
        val tripName = binding.tripName.text.toString()
        val countryName = binding.ulke.text.toString()
        val memory = binding.ani.text.toString()

        if (tripName.isNotEmpty() && countryName.isNotEmpty() && memory.isNotEmpty() && selectedBitmaps.isNotEmpty()) {
            try {
                // Trip bilgilerini ekle
                val tripStatement = database.compileStatement("INSERT INTO trip ( tripName , ulke , ani ) VALUES (?, ?, ?)")
                tripStatement.bindString(1, tripName)
                tripStatement.bindString(2, countryName)
                tripStatement.bindString(3, memory)

                val tripId = tripStatement.executeInsert()

                if (tripId != -1L) {
                    // Resimleri ekle
                    val resimStatement = database.compileStatement("INSERT INTO resimler (trip_id, image) VALUES (?, ?)")
                    for (bitmap in selectedBitmaps) {
                        val smallBitmap = makeSmallerBitmap(bitmap, 300)
                        val outputStream = ByteArrayOutputStream()
                        smallBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                        val byteArray = outputStream.toByteArray()

                        resimStatement.bindLong(1, tripId)
                        resimStatement.bindBlob(2, byteArray)
                        resimStatement.execute()
                    }
                    Toast.makeText(this, "Gezi başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AnaSayfa::class.java)

                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Trip ID oluşturulamadı.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("Hata: ${e.localizedMessage}")
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
