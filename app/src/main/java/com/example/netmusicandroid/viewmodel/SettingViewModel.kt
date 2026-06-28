package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.netmusicandroid.data.repository.AuthRepository

class SettingViewModel : ViewModel() {
    // 吐司提示消息
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    private val authRepo = AuthRepository.getInstance()

    // 跳转修改个人信息页面
    fun goModifyUserInfo() {
        _toastMsg.postValue("前往修改个人信息页面")
    }

    // 退出登录逻辑
    fun logoutAction() {
        authRepo.logout()
        _toastMsg.postValue("已退出登录")
    }

    // 跳转修改主题页面
    fun goModifyTheme() {
        _toastMsg.postValue("前往主题设置页面")
    }
}