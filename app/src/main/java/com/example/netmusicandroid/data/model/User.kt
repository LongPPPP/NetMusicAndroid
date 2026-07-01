package com.example.netmusicandroid.data.model

//各响应体的具体定义
data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    val avatar: String?,
    val signature: String?,
    val role: String?,
    val createdAt: String?,
    val comment_count: Int = 0,
    val favorite_count: Int = 0
)

data class LoginData(
    val user_id: Int,
    val access_token: String,
    val refresh_token: String,
    val user: UserInfo
)

data class RefreshTokenData(
    val access_token: String,
    val expires_in: Int
)