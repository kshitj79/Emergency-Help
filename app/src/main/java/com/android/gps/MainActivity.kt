package com.android.gps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


var username = ""
var password = ""


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var  login = findViewById<Button>(R.id.login)
        var id = findViewById<EditText>(R.id.usr)
        var pwd = findViewById<EditText>(R.id.pwd)

        login.setOnClickListener {

            username = id.text.toString().trim()
            password = pwd.text.toString().trim()
            Login(username)


        }


    }


    fun Login(id:String){
        if(username=="test")
            if(password=="test")
            {
                val inttent = Intent(this, MainActivity2::class.java)
                startActivity(inttent)
            }
    }
}