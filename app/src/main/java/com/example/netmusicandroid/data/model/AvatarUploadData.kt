package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/** 头像上传响应 — PUT /users/me/avatar 返回 {url: "/static/avatars/xxx.jpg"} */
@JsonClass(generateAdapter = true)
data class AvatarUploadData(
    val url: String
)
