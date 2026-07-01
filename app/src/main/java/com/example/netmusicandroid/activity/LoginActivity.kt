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

        // MVVM: 观察最近登录用户，自动填充
        viewModel.lastUser.observe(this) { user ->
            user?.let {
                etEmail.setText(it.email)
                etPassword.setText(it.password)
            }
        }
        viewModel.fetchLastUser()

        // MVVM: 观察登录结果
        viewModel.loginResult.observe(this) { (success, message) ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) {
                startActivity(Intent(this, BaseActivity::class.java))
                finish()
            }
        }

        // MVVM: 观察历史记录列表，展示弹窗
        viewModel.historyUsers.observe(this) { users ->
            if (users.isNotEmpty()) {
                showAccountHistoryPopup(ivShowHistory, users)
            } else {
                Toast.makeText(this, "暂无历史登录记录", Toast.LENGTH_SHORT).show()
            }
        }

        ivShowHistory.setOnClickListener {
            viewModel.fetchAllHistoryUsers()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            viewModel.login(email, password)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showAccountHistoryPopup(anchor: View, users: List<com.example.netmusicandroid.data.db.UserEntity>) {
        // 创建弹窗内容
        val popupView = LayoutInflater.from(this).inflate(R.layout.layout_history_popup, null)
        val rv = popupView.findViewById<RecyclerView>(R.id.rvHistory)
        
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
                viewModel.deleteLocalUser(user)
                popup.dismiss()
                Toast.makeText(this, "已删除该账号记录", Toast.LENGTH_SHORT).show()
            }
        )
        
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        
        popup.showAsDropDown(container, 0, 5)
    }
}
