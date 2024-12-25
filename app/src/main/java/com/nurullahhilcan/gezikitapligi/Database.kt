package com.nurullahhilcan.gezikitapligi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context): SQLiteOpenHelper(context, "Geziler", null, 1) {
    // Database Oluşturma Fonksiyonu
    override fun onCreate(db: SQLiteDatabase?) {

        val createUsersTable = """CREATE TABLE users(
        |   id INTEGER PRIMARY KEY AUTOINCREMENT,
        |   name TEXT NOT NULL,
        |   surname TEXT NOT NULL,
        |   username TEXT NOT NULL UNIQUE,
        |   email TEXT NOT NULL UNIQUE,
        |   password TEXT NOT NULL
        |)""".trimMargin()
        db?.execSQL(createUsersTable)

    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // Kullanıcı Ekleme Fonksiyonu
    fun addUser(name:String, surname:String,username: String, email: String, password: String,): Boolean {

        val db = writableDatabase
        val insertQuery = """
            INSERT INTO users (name, surname, username, email,password)
            VALUES ('$name', '$surname', '$username', '$email','$password')
        """


        return try {
            db.execSQL(insertQuery)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    // Giriş Doğrulama Fonksiyonu
    fun isUserValid(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM users
            WHERE username = '$username' AND password = '$password'
        """
        val cursor = db.rawQuery(query, null)
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }
    //Kullanıcı id'sini alma fonksiyonu
    fun getUser(username: String): Users? {
        val db = readableDatabase
        val query = """
        SELECT * FROM Users
        WHERE username = '$username'
    """
        val cursor = db.rawQuery(query, null)

        val idIx = cursor.getColumnIndex("id")  // 'id' sütununun index'ini alıyoruz
        val nameIx = cursor.getColumnIndex("name")
        val surnameIx = cursor.getColumnIndex("surname")
        val usernameIx = cursor.getColumnIndex("username")
        val emailIx = cursor.getColumnIndex("email")
        val passwordIx = cursor.getColumnIndex("password")

        var user: Users? = null

        if (cursor.moveToFirst()) {
            // Cursor'dan verileri alıyoruz
            val id = cursor.getInt(idIx)  // Kullanıcı ID'sini alıyoruz
            val name = cursor.getString(nameIx)
            val surname = cursor.getString(surnameIx)
            val username = cursor.getString(usernameIx)
            val email = cursor.getString(emailIx)
            val password = cursor.getString(passwordIx)

            // Users nesnesini oluşturuyoruz
            user = Users(id, name, surname, username, email, password)
        }
        cursor.close()  // Cursor'ı kapatıyoruz
        return user  // Kullanıcıyı geri döndürüyoruz
    }


}