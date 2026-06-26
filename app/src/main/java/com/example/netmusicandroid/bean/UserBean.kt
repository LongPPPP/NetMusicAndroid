package com.example.netmusicandroid.bean

/**
 * 获取用户信息接口 data 内部实体
 * 匹配后端返回data结构
 */
data class UserBean(
    val id: Int,                  // 用户唯一ID，后端是数字1
    val username: String,         // 用户名，非空
    val email: String,            // 邮箱
    val avatar: String?,          // 头像，可为null
    val signature: String?,      // 个性签名，可为null
    val role: String,             // 角色 USER
    val createdAt: String         // 创建时间
)