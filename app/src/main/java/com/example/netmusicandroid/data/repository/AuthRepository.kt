package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.AuthApiService
import com.example.netmusicandroid.data.api.LoginRequest
import com.example.netmusicandroid.data.api.RefreshRequest
import com.example.netmusicandroid.data.api.RegisterRequest
import com.example.netmusicandroid.data.db.UserDao
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.sp.SpManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AuthRepository private constructor(
    private val userDao: UserDao
) {
    private val api = ApiClient.createService<AuthApiService>()

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        // 只允许Application启动时调用一次带参初始化
        fun initRepo(userDao: UserDao) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = AuthRepository(userDao)
                    }
                }
            }
        }
        // 所有ViewModel/页面统一调用无参getInstance，不再传Dao
        fun getInstance(): AuthRepository {
            return INSTANCE ?: throw IllegalStateException("AuthRepository未在Application初始化！请先调用initRepo")
        }
    }

    suspend fun login(
        email: String,
        password: String
    ) = api.login(LoginRequest(email, password)).also { response ->
        if (response.code == 200 && response.data != null) {
            val loginData = response.data
            val now = System.currentTimeMillis()
            val tokenExpireTime = now + 900 * 1000
            val userEntity = UserEntity(
                email = loginData.user.email,
                username = loginData.user.username,
                password = password,
                lastLoginTime = now,
                avatar = loginData.user.avatar ?: "",
                signature = loginData.user.signature ?: "",
                role = loginData.user.role ?: "",
                accessToken = loginData.access_token,
                refreshToken = loginData.refresh_token,
                tokenExpire = tokenExpireTime
            )
            userDao.saveUser(userEntity)
            SpManager.setCurrentLoginEmail(loginData.user.email)
            SpManager.setLoginStatus(true)
            SpManager.setUserId(loginData.user.id.toLong())
        }
    }

    suspend fun register(
        username: String,
        password: String,
        confirmPassword: String,
        email: String
    ) = api.register(RegisterRequest(username, password, confirmPassword, email))

    suspend fun refreshAccessToken(): Result<String> {
        return try {
            val loginEmail = SpManager.getCurrentLoginEmail()
                ?: return Result.failure(Throwable("未登录，请重新登录"))
            val user = userDao.findUserByEmail(loginEmail)
                ?: return Result.failure(Throwable("本地账号信息丢失，请重新登录"))
            val refreshToken = user.refreshToken
            if (refreshToken.isBlank()) {
                return Result.failure(Throwable("无有效刷新凭证，请重新登录"))
            }
            val res = api.refreshToken(RefreshRequest(refreshToken))
            if (res.code != 200 || res.data == null) {
                // 刷新失败，清空双Token，避免循环无效请求
                val clearUser = user.copy(accessToken = "", refreshToken = "", tokenExpire = 0L)
                userDao.saveUser(clearUser)
                return Result.failure(Throwable("RefreshToken已失效，请重新登录"))
            }
            val refreshData = res.data
            val newExpireTime = System.currentTimeMillis() + refreshData.expires_in * 1000
            val updateUser = user.copy(
                accessToken = refreshData.access_token,
                tokenExpire = newExpireTime
            )
            userDao.saveUser(updateUser)
            Result.success(refreshData.access_token)
        } catch (e: Exception) {
            // 网络/解析异常，清空过期accessToken
            val loginEmail = SpManager.getCurrentLoginEmail()
            loginEmail?.let { email ->
                userDao.findUserByEmail(email)?.let { user ->
                    userDao.saveUser(user.copy(accessToken = ""))
                }
            }
            Result.failure(e)
        }
    }

    suspend fun getValidAccessToken(): Result<String> {
        val loginEmail = SpManager.getCurrentLoginEmail()
            ?: return Result.failure(Throwable("未登录，请前往登录"))
        val user = userDao.findUserByEmail(loginEmail)
            ?: return Result.failure(Throwable("本地用户数据不存在，请重新登录"))
        val now = System.currentTimeMillis()
        // Token未过期直接返回
        if (now < user.tokenExpire) {
            return Result.success(user.accessToken)
        }
        // 已过期自动执行刷新
        return refreshAccessToken()
    }

    suspend fun logout() {
        val loginEmail = SpManager.getCurrentLoginEmail() ?: return
        val user = userDao.findUserByEmail(loginEmail) ?: return
        // 数据库清空双Token与过期时间
        userDao.saveUser(user.copy(accessToken = "", refreshToken = "", tokenExpire = 0L))
        // 同步清空SP，保证拦截器立刻读取到登出状态
        SpManager.clearAllSync()
        SpManager.setLoginStatus(false)
        SpManager.setUserId(0)
    }

    suspend fun getCurrentLoginUser(): UserEntity? {
        val email = SpManager.getCurrentLoginEmail() ?: return null
        return userDao.findUserByEmail(email)
    }


    fun observeCurrentLoginUser(): Flow<UserEntity?> {
        val loginEmail = SpManager.getCurrentLoginEmail()
        return if (loginEmail.isNullOrBlank()) {
            flowOf(null)
        } else {
            userDao.observeUserByEmail(loginEmail)
        }
    }

    fun isLogin(): Boolean = SpManager.getLoginStatus()
}