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
        // HomeFragment 是 Fragment，不是 Activity，不能通过 startActivity() 跳转；
        // 应启动 BaseActivity，再由 BaseActivity 加载 HomeFragment。
        btnLogin.setOnClickListener {

            startActivity(
                Intent(this, BaseActivity::class.java)
            )

            finish()
        }

        tvRegister.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )

        }
    }
}