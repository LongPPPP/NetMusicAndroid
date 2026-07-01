package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository.getInstance()

    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.register(username, password, confirmPassword, email)
                onResult(result.code == 200 || result.code == 201, result.message ?: "")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    JSONObject(errorBody ?: "").getString("message")
                } catch (ex: Exception) {
                    "请求失败"
                }
                onResult(false, message)
            } catch (e: Exception) {
                onResult(false, e.message ?: "网络异常")
            }
        }
    }
}