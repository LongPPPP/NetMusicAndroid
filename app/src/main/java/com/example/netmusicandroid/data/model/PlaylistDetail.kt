package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistDetailData(
    val playlist_id: Int,
    val playlist_name: String,
    val user_id: Int,
    val cover_url: String?,
    val created_at: String,
    val songs: List<SongItem>
)
