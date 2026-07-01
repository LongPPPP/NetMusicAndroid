package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository.getInstance()

    private val _registerResult = MutableLiveData<Pair<Boolean, String>>()
    val registerResult: LiveData<Pair<Boolean, String>> = _registerResult

    fun register(username: String, password: String, confirmPassword: String, email: String) {
        viewModelScope.launch {
            // 统一使用 Result 风格，逻辑更简洁
            val result = repository.register(username, password, confirmPassword, email)
            
            result.onSuccess {
                _registerResult.value = Pair(true, "注册成功")
            }
            
            result.onFailure { error ->
                _registerResult.value = Pair(false, error.message ?: "注册失败")
            }
        }
    }
}
