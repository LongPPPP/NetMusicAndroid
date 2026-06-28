package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.SingerDetail
import com.example.netmusicandroid.data.model.SingerListData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// 歌手相关接口
interface SingerApiService {

    // 分页获取歌手列表
    @GET("singers")
    suspend fun getSingers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<SingerListData>

    // 获取歌手详情（含热门歌曲）
    @GET("singers/{singerId}")
    suspend fun getSingerDetail(
        @Path("singerId") singerId: Int
    ): ApiResponse<SingerDetail>
}
