package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/** 歌曲列表项 */
@JsonClass(generateAdapter = true)
data class SongItem(
    val song_id: Int,
    val song_name: String,
    val singer_name: String,
    val play_url: String?,
    val duration: Int?
)

/** 歌曲详情 */
@JsonClass(generateAdapter = true)
data class SongDetail(
    val song_id: Int,
    val song_name: String,
    val singer_name: String,
    val play_url: String?,
    val cover_url: String?,
    val duration: Int?
)

/** 歌曲列表分页数据 */
@JsonClass(generateAdapter = true)
data class SongListData(
    val list: List<SongItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)