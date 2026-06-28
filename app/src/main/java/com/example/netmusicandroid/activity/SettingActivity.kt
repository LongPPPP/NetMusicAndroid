package com.example.netmusicandroid.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.netmusicandroid.databinding.ActivitySettingBinding
import com.example.netmusicandroid.viewmodel.SettingViewModel
import com.example.netmusicandroid.utils.ToastUtil

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var settingVm: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initObserver()
        initClick()
    }

    private fun initViewModel() {
        settingVm = ViewModelProvider(this)[SettingViewModel::class.java]
    }

    private fun initObserver() {
        // 监听吐司消息
        settingVm.toastMsg.observe(this) { msg ->
            ToastUtil.showShort(msg)
        }
    }

    private fun initClick() {
        // 返回上一页
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 修改个人信息
        binding.rlItemModifyInfo.setOnClickListener {
            settingVm.goModifyUserInfo()
        }

        // 退出登录
        binding.rlItemLogout.setOnClickListener {
            settingVm.logoutAction()
        }

        // 修改主题
        binding.rlItemTheme.setOnClickListener {
            settingVm.goModifyTheme()
        }
    }
}