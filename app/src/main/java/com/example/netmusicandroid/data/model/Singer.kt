package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SingerItem(
    val singer_id: Int,
    val singer_name: String,
    val avatar_url: String?
)

@JsonClass(generateAdapter = true)
data class SingerDetail(
    val singer_id: Int,
    val singer_name: String,
    val avatar_url: String?,
    val description: String?,
    val hot_songs: List<HotSong>
)

@JsonClass(generateAdapter = true)
data class HotSong(
    val song_id: Int,
    val song_name: String
)

@JsonClass(generateAdapter = true)
data class SingerListData(
    val list: List<SingerItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)