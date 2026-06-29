package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.PlaylistDetailApiService
import com.example.netmusicandroid.data.model.PlaylistDetailData

class PlaylistDetailRepository {
    private val playlistDetailApi = ApiClient.createService<PlaylistDetailApiService>()

    suspend fun getPlaylistDetail(playlistId: Int): Result<PlaylistDetailData> {
        return try {
            val resp = playlistDetailApi.getPlaylistDetail(playlistId)
            if (resp.isSuccessful) {
                val apiResponse = resp.body()
                when {
                    apiResponse == null -> {
                        Result.failure(RuntimeException("返回数据为空"))
                    }
                    apiResponse.code != 200 -> {
                        Result.failure(RuntimeException("接口异常，错误码：${apiResponse.code}"))
                    }
                    apiResponse.data == null -> {
                        Result.failure(RuntimeException("歌单详情数据为空"))
                    }
                    else -> {
                        Result.success(apiResponse.data)
                    }
                }
            } else {
                Result.failure(RuntimeException("请求失败，HTTP状态码：${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSongFromPlaylist(playlistId: Int, songId: Int): Result<Unit> {
        return try {
            val resp = playlistDetailApi.deleteSong(playlistId, songId)
            if (resp.isSuccessful) {
                val apiResp = resp.body()
                if (apiResp?.code == 200) {
                    Result.success(Unit)
                } else {
                    Result.failure(RuntimeException(apiResp?.message ?: "移除歌曲失败"))
                }
            } else {
                Result.failure(RuntimeException("网络请求失败：${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
