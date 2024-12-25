package com.nurullahhilcan.gezikitapligi

import java.io.Serializable

class Users(val id:Int,val name:String,val surname:String,val username:String,val email:String,val password:String):
    Serializable {
}