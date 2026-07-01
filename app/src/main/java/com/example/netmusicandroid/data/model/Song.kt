package com.example.netmusicandroid.data.model



// 歌曲详情 
data class SongDetail(
    val song_id: Int,
    val song_name: String,
    val singer_name: String,
    val play_url: String?,
    val cover_url: String?,
    val duration: Int?
)

// 歌曲列表分页数据 
data class SongListData(
    val list: List<SongDetail>,
    val total: Int,
    val page: Int,
    val page_size: Int
)