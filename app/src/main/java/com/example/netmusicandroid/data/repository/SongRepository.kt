package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.model.SongItem

/**
 * 歌曲数据仓库。
 *
 * Repository 的作用：
 * 1. 调用 API
 * 2. 把响应包装成 Result<T>（成功/失败），ViewModel 不需要知道 Retrofit 的存在
 */
class SongRepository {

    private val api = ApiClient.createService<SongApiService>()

    /** 获取歌曲列表 */
    suspend fun fetchSongs(page: Int = 1): Result<List<SongItem>> = runCatching {
        val response = api.getSongs(page)
        // 检查业务层状态码
        if (response.code != 200) {
            throw Exception(response.message ?: "请求失败")
        }
        response.data?.list ?: emptyList()
    }

    /** 获取歌曲详情（含 play_url） */
    suspend fun fetchSongDetail(songId: Int): Result<SongDetail> = runCatching {
        val response = api.getSongDetail(songId)
        if (response.code != 200) {
            throw Exception(response.message ?: "请求失败")
        }
        response.data ?: throw Exception("歌曲不存在")
    }

    /** 构建可播放的完整 URL */
    fun buildPlayUrl(playUrl: String, baseHost: String = "http://10.0.2.2:3000"): String {
        return if (playUrl.startsWith("http")) {
            playUrl   // 已经是完整 URL
        } else {
            "$baseHost$playUrl"   // 相对路径 → 补全
        }
    }
}