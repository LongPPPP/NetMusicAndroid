package com.example.netmusicandroid.data.model


// 歌手列表简项
data class SingerItem(
    val singer_id: Int,
    val singer_name: String,
    val avatar_url: String?
)

// 歌手详情 (对应 /singers/{singerId})
data class SingerDetail(
    val singer_id: Int,
    val singer_name: String,
    val avatar_url: String?,
    val description: String?,
    val hot_songs: List<HotSong>
)

// 歌手详情中的歌曲简项
data class HotSong(
    val song_id: Int,
    val song_name: String
)

// 歌手列表分页包装
data class SingerListData(
    val list: List<SingerItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)
