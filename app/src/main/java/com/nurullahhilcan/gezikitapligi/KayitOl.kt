package com.nurullahhilcan.gezikitapligi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nurullahhilcan.gezikitapligi.databinding.ActivityGirisBinding
import com.nurullahhilcan.gezikitapligi.databinding.ActivityKayitOlBinding

class KayitOl : AppCompatActivity() {
    private lateinit var binding: ActivityKayitOlBinding
    private lateinit var databaseHelper: Database
    override fun onCreate(savedInstanceState: Bundle?) {
        //veritabanı bağlantısı
        databaseHelper = Database(this)

        super.onCreate(savedInstanceState)
        binding = ActivityKayitOlBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Database(this)
        binding.kayitButon.setOnClickListener{
            val name = binding.kayitIsim.text.toString()
            val surname = binding.kayitSoyisim.text.toString()
            val username = binding.kayitKullaniciAdi.text.toString()
            val email = binding.kayitEposta.text.toString()
            val password = binding.kayitSifre.text.toString()
            if(binding.kayitSifre.text.toString()!=binding.kayitSifreTekrar.text.toString()){
                Toast.makeText(this, "Şifreler Uyuşmuyor", Toast.LENGTH_SHORT).show()
            }
            else{
                if(name.isNotEmpty()&&surname.isNotEmpty()&&username.isNotEmpty()&&email.isNotEmpty()&&password.isNotEmpty()){
                    val isInserted = databaseHelper.addUser(name,surname,username,email,password)
                    if(isInserted){
                        Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                        finish()
                    }else{
                        Toast.makeText(this, "Kayıt Başarısız! Email veya Kullanıcı Adı Zaten Var.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this,"Lütfen tüm alanları doldurun!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}