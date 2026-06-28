package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SingerApiService
import com.example.netmusicandroid.data.model.SingerDetail
import com.example.netmusicandroid.data.model.SingerItem
import org.json.JSONObject
import retrofit2.HttpException

class SingerRepository {

    private val api = ApiClient.createService<SingerApiService>()

    // 统一处理错误解析
    private fun parseError(e: Throwable): String {
        return if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                e.message() ?: "请求错误"
            }
        } else {
            e.message ?: "网络异常"
        }
    }

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
