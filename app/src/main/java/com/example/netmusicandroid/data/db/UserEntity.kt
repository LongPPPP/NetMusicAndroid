package com.example.netmusicandroid.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val username: String,
    val password: String,
    val lastLoginTime: Long,
    val avatar: String,
    val signature: String,
    val role: String,

    // 统计数据
    val commentCount: Int = 0,
    val favoriteCount: Int = 0,

    // 登录返回鉴权凭证
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenExpire: Long = 0L // access_token过期时间戳（后端exp）
)