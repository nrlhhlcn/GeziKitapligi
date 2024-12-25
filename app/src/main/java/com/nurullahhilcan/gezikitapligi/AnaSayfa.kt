package com.nurullahhilcan.gezikitapligi

import android.content.Context
import android.content.Intent
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
    private lateinit var artList : ArrayList<Trip>
    private lateinit var artAdapter : TripAdaptor
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
        artList = ArrayList<Trip>()
        artAdapter = TripAdaptor(artList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter

        try {

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            database.execSQL("CREATE TABLE IF NOT EXISTS trip (id INTEGER PRIMARY KEY, tripName VARCHAR,ulke VARCHAR,ani VARCHAR)")

            val cursor = database.rawQuery("SELECT * FROM trip",null)
            val trimNameIx = cursor.getColumnIndex("tripName")
            val ulkeIx = cursor.getColumnIndex("ulke")
            val aniIx = cursor.getColumnIndex("ani")

            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val tripName = cursor.getString(trimNameIx)
                val id = cursor.getInt(idIx)
                val ulke=cursor.getString(ulkeIx)
                val ani=cursor.getString(aniIx)

                val art = Trip(tripName,ulke,ani)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun geziEkle(view: View){
        var intent= Intent(this@AnaSayfa,GeziEkle::class.java)
        startActivity(intent)
    }
}