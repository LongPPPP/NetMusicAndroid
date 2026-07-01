package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SingerApiService
import com.example.netmusicandroid.data.model.SingerDetail
import com.example.netmusicandroid.data.model.SingerItem

class SingerRepository private constructor() {

    private val api = ApiClient.createService<SingerApiService>()

    companion object {
        @Volatile
        private var INSTANCE: SingerRepository? = null

        fun getInstance(): SingerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SingerRepository().also { INSTANCE = it }
            }
        }
    }

    private fun parseError(e: Throwable): String = RepositoryErrorParser.parse(e)

    // 获取歌手详情
    suspend fun fetchSingerDetail(singerId: Int): Result<SingerDetail> = try {
        val response = api.getSingerDetail(singerId)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "获取歌手详情失败"))
        } else {
            Result.success(response.data ?: throw Exception("歌手不存在"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    // 获取歌手列表
    suspend fun fetchSingers(page: Int = 1): Result<List<SingerItem>> = try {
        val response = api.getSingers(page)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "获取歌手列表失败"))
        } else {
            Result.success(response.data?.list ?: emptyList())
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }
}
