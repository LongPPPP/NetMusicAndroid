package com.example.netmusicandroid.data.model


data class SongItem(
    val song_id: Int,
    val song_name: String,
    val singer_id: Int,
    val singer_name: String,
    val cover_url: String?,
    val play_url: String,
    val duration: Int,
    val added_at: String
)