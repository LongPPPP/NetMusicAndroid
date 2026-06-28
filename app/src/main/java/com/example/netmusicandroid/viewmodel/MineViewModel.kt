package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MineViewModel : ViewModel() {
    // 全局单例仓库
    private val authRepository = AuthRepository.getInstance()

    val currentUserFlow: Flow<UserEntity?> = authRepository.observeCurrentLoginUser()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}