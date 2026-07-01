package com.example.netmusicandroid.data.model


/** 收藏歌单详情 — GET /users/me/favorites 返回的 data 字段 */
data class FavoriteData(
    val playlist_id: Int,
    val playlist_name: String,
    val user_id: Int,
    val songs: List<SongItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)