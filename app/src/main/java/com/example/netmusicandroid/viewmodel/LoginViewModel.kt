package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.db.UserEntity

class LoginViewModel : ViewModel() {
    private val userDao = AppDatabase.globalUserDao
    private val authRepository = AuthRepository.getInstance()

    // 登录结果状态：Pair<是否成功, 提示消息>
    private val _loginResult = MutableLiveData<Pair<Boolean, String>>()
    val loginResult: LiveData<Pair<Boolean, String>> = _loginResult

    // 历史用户列表
    private val _historyUsers = MutableLiveData<List<UserEntity>>()
    val historyUsers: LiveData<List<UserEntity>> = _historyUsers

    // 最近登录用户
    private val _lastUser = MutableLiveData<UserEntity?>()
    val lastUser: LiveData<UserEntity?> = _lastUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Repository 返回 Result<LoginData>，内部已处理异常
            val result = authRepository.login(email, password)
            
            result.onSuccess {
                _loginResult.value = Pair(true, "登录成功")
            }
            
            result.onFailure { error ->
                _loginResult.value = Pair(false, error.message ?: "登录失败")
            }
        }
    }

    fun fetchAllHistoryUsers() {
        viewModelScope.launch {
            _historyUsers.value = userDao.getAllHistoryUsers()
        }
    }

    fun deleteLocalUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.deleteUser(user)
            fetchAllHistoryUsers()
        }
    }

    fun fetchLastUser() {
        viewModelScope.launch {
            val users = userDao.getAllHistoryUsers()
            _lastUser.value = users.firstOrNull()
        }
    }
}
