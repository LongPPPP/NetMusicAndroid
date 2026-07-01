package com.example.netmusicandroid.data.model


/** 头像上传响应 — PUT /users/me/avatar 返回 {url: "/static/avatars/xxx.jpg"} */
data class AvatarUploadData(
    val url: String
)
