package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

/**
 * 歌曲搜索结果项 — 字段少于 SongDetail，不含 play_url / duration。
 * 点击需通过 SongRepository.fetchSongDetail(song_id) 获取完整信息后播放。
 */
@JsonClass(generateAdapter = true)
data class SearchSongItem(
    val song_id: Int,
    val song_name: String,
    val singer_id: Int,
    val singer_name: String,
    val cover_url: String?
)

/** 歌曲搜索分页列表 — 复用现有 SongListData / SingerListData 模式 */
@JsonClass(generateAdapter = true)
data class SearchSongListData(
    val list: List<SearchSongItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)

/**
 * 歌单搜索分页列表。
 * list 元素字段 (playlist_id, playlist_name, song_count, cover_url) 与 UserPlaylist 完全一致，
 * 直接复用 UserPlaylist 类型，无需新建 item 类。
 */
@JsonClass(generateAdapter = true)
data class PlaylistSearchListData(
    val list: List<UserPlaylist>,
    val total: Int,
    val page: Int,
    val page_size: Int
)
