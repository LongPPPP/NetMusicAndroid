package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/** 创建歌单POST请求body */
@JsonClass(generateAdapter = true)
data class CreatePlaylistReq(
    val name: String
)