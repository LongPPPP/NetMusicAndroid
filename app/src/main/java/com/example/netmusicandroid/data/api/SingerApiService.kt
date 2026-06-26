package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.SingerDetail
import com.example.netmusicandroid.data.model.SingerListData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SingerApiService {

    @GET("singers")
    suspend fun getSingers(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<SingerListData>

    @GET("singers/{singerId}")
    suspend fun getSingerDetail(
        @Path("singerId") singerId: Int
    ): ApiResponse<SingerDetail>
}