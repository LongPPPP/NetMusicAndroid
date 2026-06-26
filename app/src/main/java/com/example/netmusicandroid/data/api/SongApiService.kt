package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.model.SongListData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 歌曲相关接口
 *
 * 所有方法都是 suspend 函数，在协程里调用不会阻塞主线程。
 */
interface SongApiService {

    /** 分页获取歌曲列表（可选按歌手筛选） */
    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("singer_id") singerId: Int? = null   // 可选：按歌手 ID 筛选
    ): ApiResponse<SongListData>

    /** 获取歌曲详情（含 play_url） */
    @GET("songs/{songId}")
    suspend fun getSongDetail(
        @Path("songId") songId: Int
    ): ApiResponse<SongDetail>
}