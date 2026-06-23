package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.netmusicandroid.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {

            startActivity(
                Intent(this, HomeActivity::class.java)
            )

        }

        tvRegister.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )

        }
    }
}