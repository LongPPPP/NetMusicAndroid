package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreatePlaylistResp(
    val playlist_id: Int
)