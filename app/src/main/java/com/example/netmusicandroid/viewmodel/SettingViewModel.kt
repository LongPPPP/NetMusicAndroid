package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.io.File

class SettingViewModel : ViewModel() {

    private val authRepo = AuthRepository.getInstance()

    // ── Toast ───────────────────────────────────

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    // ── 当前用户（预填弹窗） ─────────────────────

    private val _currentUser = MutableLiveData<UserEntity?>()
    val currentUser: LiveData<UserEntity?> = _currentUser

    fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.postValue(authRepo.getCurrentLoginUser())
        }
    }

    // ── 修改个人信息（suspend，由调用方保证顺序执行）──

    /** 更新单个用户字段（username / signature），成功后将最新 UserEntity 同步回 LiveData */
    suspend fun updateUserField(field: String, value: String) {
        val result = authRepo.updateUser(field, value)
        result.onSuccess { updatedUser ->
            _currentUser.postValue(updatedUser)
            _toastMsg.postValue("修改成功")
        }.onFailure { e ->
            _toastMsg.postValue(e.message ?: "修改失败")
        }
    }

    /** 上传头像，成功后将最新 UserEntity 同步回 LiveData */
    suspend fun uploadAvatar(file: File) {
        val result = authRepo.uploadAvatar(file)
        result.onSuccess { updatedUser ->
            _currentUser.postValue(updatedUser)
            _toastMsg.postValue("头像修改成功")
        }.onFailure { e ->
            _toastMsg.postValue(e.message ?: "头像上传失败")
        }
    }

    // ── 原有方法 ────────────────────────────────

    /** 触发弹出修改个人信息弹窗（由 Activity 监听此 LiveData 弹出 Dialog） */
    fun goModifyUserInfo() {
        _toastMsg.postValue("") // 仅作触发标记，实际 Dialog 在 Activity 中弹出
    }

    suspend fun logoutAction() {
        authRepo.logout()
        _toastMsg.postValue("已退出登录")
    }

    fun goModifyTheme() {
        _toastMsg.postValue("前往主题设置页面")
    }
}
