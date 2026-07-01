package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/** GET /users/me/singer 响应 data 字段 */
@JsonClass(generateAdapter = true)
data class SingerIdData(
    val singer_id: Int
)
