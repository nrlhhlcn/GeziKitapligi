package com.nurullahhilcan.gezikitapligi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context): SQLiteOpenHelper(context, "UserDB", null, 1) {
    // Database Oluşturma Fonksiyonu
    override fun onCreate(db: SQLiteDatabase?) {

        val createTableQuery = """CREATE TABLE Users(
        |   id INTEGER PRIMARY KEY AUTOINCREMENT,
        |   name TEXT NOT NULL,
        |   surname TEXT NOT NULL,
        |   username TEXT NOT NULL UNIQUE,
        |   email TEXT NOT NULL UNIQUE,
        |   password TEXT NOT NULL
        |)""".trimMargin()
            db?.execSQL(createTableQuery)

        }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)
    }

    // Kullanıcı Ekleme Fonksiyonu
    fun addUser(name:String, surname:String,username: String, email: String, password: String,): Boolean {

        val db = writableDatabase
        val insertQuery = """
            INSERT INTO Users (name, surname, username, email,password)
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
            SELECT * FROM Users
            WHERE username = '$username' AND password = '$password'
        """
        val cursor = db.rawQuery(query, null)
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }


}