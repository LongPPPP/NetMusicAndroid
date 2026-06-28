package com.example.netmusicandroid.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.HistoryAccountAdapter
import com.example.netmusicandroid.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val ivShowHistory = findViewById<ImageView>(R.id.ivShowHistory)

        // 1. 自动填充上次登录
        viewModel.getLastUser { user ->
            if (user != null) {
                etEmail.setText(user.email)
                etPassword.setText(user.password)
            }
        }

        // 2. 点击箭头显示历史记录弹窗
        ivShowHistory.setOnClickListener {
            showAccountHistoryPopup(ivShowHistory)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            viewModel.login(email, password) { success, message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    startActivity(Intent(this, BaseActivity::class.java))
                    finish()
                }
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showAccountHistoryPopup(anchor: View) {
        viewModel.getAllHistoryUsers { users ->
            if (users.isEmpty()) {
                Toast.makeText(this, "暂无历史登录记录", Toast.LENGTH_SHORT).show()
                return@getAllHistoryUsers
            }

            // 创建弹窗内容
            val popupView = LayoutInflater.from(this).inflate(R.layout.layout_history_popup, null)
            val rv = popupView.findViewById<RecyclerView>(R.id.rvHistory)
            
            // 获取输入框容器的宽度，让弹窗和输入框一样宽
            val container = anchor.parent as View
            val popup = PopupWindow(popupView, container.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            
            popup.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            popup.elevation = 20f

            val adapter = HistoryAccountAdapter(users, 
                onItemClick = { user ->
                    etEmail.setText(user.email)
                    etPassword.setText(user.password)
                    popup.dismiss()
                },
                onDeleteClick = { user ->
                    viewModel.deleteLocalUser(user) {
                        popup.dismiss()
                        Toast.makeText(this, "已删除该账号记录", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = adapter
            
            // 显示在输入框容器下方
            popup.showAsDropDown(container, 0, 5)
        }
    }
}
