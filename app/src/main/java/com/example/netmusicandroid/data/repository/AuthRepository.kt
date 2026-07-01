package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.AuthApiService
import com.example.netmusicandroid.data.api.LoginRequest
import com.example.netmusicandroid.data.api.RefreshRequest
import com.example.netmusicandroid.data.api.RegisterRequest
import com.example.netmusicandroid.data.api.UpdateUserRequest
import com.example.netmusicandroid.data.api.cachedAccessToken
import com.example.netmusicandroid.data.db.UserDao
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.sp.SpManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthRepository private constructor(
    private val userDao: UserDao
) {
    private val api = com.example.netmusicandroid.data.api.ApiClient.createService<AuthApiService>()

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
                password = "",
                lastLoginTime = now,
                avatar = loginData.user.avatar ?: "",
                signature = loginData.user.signature ?: "",
                role = loginData.user.role ?: "",
                commentCount = loginData.user.comment_count,
                favoriteCount = loginData.user.favorite_count,
                accessToken = loginData.access_token,
                refreshToken = loginData.refresh_token,
                tokenExpire = tokenExpireTime
            )
            userDao.saveUser(userEntity)
            SpManager.setCurrentLoginEmail(loginData.user.email)
            SpManager.setLoginStatus(true)
            SpManager.setUserId(loginData.user.id.toLong())
            cachedAccessToken = loginData.access_token
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
                cachedAccessToken = null
                return Result.failure(Throwable("RefreshToken已失效，请重新登录"))
            }
            val refreshData = res.data
            val newExpireTime = System.currentTimeMillis() + refreshData.expires_in * 1000
            val updateUser = user.copy(
                accessToken = refreshData.access_token,
                tokenExpire = newExpireTime
            )
            userDao.saveUser(updateUser)
            cachedAccessToken = refreshData.access_token
            Result.success(refreshData.access_token)
        } catch (e: Exception) {
            // 网络/解析异常，清空过期accessToken
            val loginEmail = SpManager.getCurrentLoginEmail()
            loginEmail?.let { email ->
                userDao.findUserByEmail(email)?.let { user ->
                    userDao.saveUser(user.copy(accessToken = ""))
                }
            }
            cachedAccessToken = null
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
            cachedAccessToken = user.accessToken
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
        // 精准清除登录相关SP，保留用户配置
        SpManager.setCurrentLoginEmail("")
        SpManager.setLoginStatus(false)
        SpManager.setUserId(0)
        // 清空网络层内存缓存token
        cachedAccessToken = null
    }

    suspend fun getCurrentLoginUser(): UserEntity? {
        val email = SpManager.getCurrentLoginEmail() ?: return null
        return userDao.findUserByEmail(email)
    }

    /**
     * 修改当前登录用户信息（PATCH /users/me）。
     * 成功后将最新的 UserInfo 同步更新到 Room 本地数据库。
     */
    suspend fun updateUser(field: String, value: String): Result<UserEntity> {
        return try {
            val resp = api.updateUser(UpdateUserRequest(field, value))
            if (resp.code == 200 && resp.data != null) {
                val info = resp.data
                val email = SpManager.getCurrentLoginEmail()
                    ?: return Result.failure(Throwable("未登录"))
                val user = userDao.findUserByEmail(email)
                    ?: return Result.failure(Throwable("本地数据丢失"))
                val updated = user.copy(
                    username = info.username,
                    avatar = info.avatar ?: user.avatar,
                    signature = info.signature ?: user.signature
                )
                userDao.saveUser(updated)
                Result.success(updated)
            } else {
                Result.failure(Throwable(resp.message ?: "修改失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(file: File): Result<UserEntity> {
        return try {
            val mime = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "jpg", "jpeg" -> "image/jpeg"
                else -> "image/jpeg"
            }
            val requestBody = file.asRequestBody(mime.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val resp = api.uploadAvatar(part)
            if (resp.code == 200 && resp.data != null) {
                val email = SpManager.getCurrentLoginEmail() ?: return Result.failure(Throwable("未登录"))
                val user = userDao.findUserByEmail(email) ?: return Result.failure(Throwable("本地数据丢失"))
                val updated = user.copy(avatar = resp.data.url)
                userDao.saveUser(updated)
                Result.success(updated)
            } else {
                Result.failure(Throwable(resp.message ?: "上传失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 查询当前用户绑定的歌手ID GET /users/me/singer */
    suspend fun getMySingerId(): Result<Int> {
        return try {
            val resp = api.getMySinger()
            if (resp.code == 200 && resp.data != null) {
                Result.success(resp.data.singer_id)
            } else {
                Result.failure(Throwable(resp.message ?: "查询歌手信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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