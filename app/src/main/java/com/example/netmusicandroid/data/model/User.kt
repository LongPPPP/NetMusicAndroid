package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass
//各响应体的具体定义
@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    val avatar: String?,
    val signature: String?,
    val role: String?,
    val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class LoginData(
    val user_id: Int,
    val access_token: String,
    val refresh_token: String,
    val user: UserInfo
)

@JsonClass(generateAdapter = true)
data class RefreshTokenData(
    val access_token: String,
    val expires_in: Int
)