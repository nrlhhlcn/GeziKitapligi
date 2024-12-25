package com.nurullahhilcan.gezikitapligi

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nurullahhilcan.gezikitapligi.databinding.ActivityAnaSayfaBinding
import com.nurullahhilcan.gezikitapligi.databinding.ActivityGeziEkleBinding
import java.lang.Exception

class AnaSayfa : AppCompatActivity() {

    private lateinit var binding: ActivityAnaSayfaBinding
    private lateinit var tripList: ArrayList<Trip>
    private lateinit var resimList: ArrayList<Image>
    private lateinit var artAdapter: TripAdaptor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnaSayfaBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val user=intent.getSerializableExtra("user") as Users

        binding.textView.text=user.name


        tripList = ArrayList<Trip>()

        artAdapter = TripAdaptor(tripList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter

        try {
            val database = this.openOrCreateDatabase("Geziler", Context.MODE_PRIVATE, null)
            database.execSQL("CREATE TABLE IF NOT EXISTS trip (trip_id INTEGER PRIMARY KEY, user_id VARCHAR, tripName VARCHAR, ulke VARCHAR, ani VARCHAR)")
            database.execSQL("CREATE TABLE IF NOT EXISTS resimler (resim_id INTEGER PRIMARY KEY,user_id VARCHAR, trip_id INTEGER, image BLOB)")
            println("hata-1")
            val cursor = database.rawQuery(
                """
        SELECT trip.tripName , trip.ulke, trip.ani , resimler.image
        FROM trip
        LEFT JOIN resimler ON trip.trip_id = resimler.trip_id
       
        """, null
            )

            println("hata-2")

            val trimNameIx = cursor.getColumnIndex("tripName")
            val ulkeIx = cursor.getColumnIndex("ulke")
            val aniIx = cursor.getColumnIndex("ani")
            val imageIx = cursor.getColumnIndex("image")
            println("deneme")
            while (cursor.moveToNext()) {

                val tripName = cursor.getString(trimNameIx)
                val ulke = cursor.getString(ulkeIx)
                println(ulke)
                val ani = cursor.getString(aniIx)

                val image = if (!cursor.isNull(imageIx)) cursor.getBlob(imageIx) else null
                val bitmap =
                    if (image != null) BitmapFactory.decodeByteArray(image, 0, image.size) else null

                val trip = Trip(tripName, ulke, ani, bitmap)
                println("sadda")
                tripList.add(trip)

            }

            artAdapter.notifyDataSetChanged()
            cursor.close()

        } catch (e: Exception) {
            println("hataaaa")
            e.printStackTrace()
        }

    }

    fun geziEkle(view: View) {
        var intent = Intent(this@AnaSayfa, GeziEkle::class.java)
        startActivity(intent)
    }
}