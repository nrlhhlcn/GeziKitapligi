package com.nurullahhilcan.gezikitapligi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nurullahhilcan.gezikitapligi.databinding.ActivityGirisBinding

class Giris : AppCompatActivity() {
    private lateinit var binding: ActivityGirisBinding
    private lateinit var databaseHelper: Database
    override fun onCreate(savedInstanceState: Bundle?) {

        //veritabanı bağlantısı
        databaseHelper = Database(this)

        super.onCreate(savedInstanceState)
        binding = ActivityGirisBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.girisButon.setOnClickListener{

            val username = binding.girisKullaniciAdi.text.toString()
            val password = binding.girisSifre.text.toString()

            if(username.isNotEmpty()&&password.isNotEmpty()){
                val isValid = databaseHelper.isUserValid(username,password)

                if(isValid){
                    val user=databaseHelper.getUser(username)
                    Toast.makeText(this,"Giriş Başarılı!",Toast.LENGTH_SHORT).show()

                    val intent = Intent(this,AnaSayfa::class.java )
                    intent.putExtra("user",user)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"Hatalı Kullanıcı Adı veya Şifre!",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Lütfen tüm alanları doldurun!",Toast.LENGTH_SHORT).show()
            }

        }
        binding.girisKayitButon.setOnClickListener{
            val intent = Intent(this,KayitOl::class.java )
            startActivity(intent)
        }
    }
}