package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/**
 * 用户歌单实体，匹配接口 /api/v1/users/{userId}/playlists 返回JSON结构
 */
@JsonClass(generateAdapter = true)
data class UserPlaylist(
    val playlist_id: Int,
    val playlist_name: String,
    val song_count: Int,
    val created_at: String,
    val cover_url: String?
)