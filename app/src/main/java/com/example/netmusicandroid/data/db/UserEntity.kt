package com.example.netmusicandroid.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
  本地用户信息表
  用于实现：多账号登录记忆、记住密码、自动填充
 */
@Entity(tableName = "local_users")
data class UserEntity(
    @PrimaryKey 
    val email: String,        // 以邮箱作为主键，因为它是唯一的
    val username: String,
    val password: String,
    val lastLoginTime: Long   //最近登录的排在上面
)
