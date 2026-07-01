package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SearchApiService
import com.example.netmusicandroid.data.model.PlaylistSearchListData
import com.example.netmusicandroid.data.model.SearchSongListData
import com.example.netmusicandroid.data.model.SingerListData

class SearchRepository private constructor() {
    private val api = ApiClient.createService<SearchApiService>()

    companion object {
        @Volatile
        private var INSTANCE: SearchRepository? = null

        fun getInstance(): SearchRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SearchRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun searchSongs(keyword: String, page: Int = 1, pageSize: Int = 20): Result<SearchSongListData> = try {
        val response = api.searchSongs(keyword, page, pageSize)
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "歌曲搜索失败"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(RepositoryErrorParser.parse(e)))
    }

    suspend fun searchSingers(keyword: String, page: Int = 1, pageSize: Int = 20): Result<SingerListData> = try {
        val response = api.searchSingers(keyword, page, pageSize)
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "歌手搜索失败"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(RepositoryErrorParser.parse(e)))
    }

    suspend fun searchPlaylists(keyword: String, page: Int = 1, pageSize: Int = 20): Result<PlaylistSearchListData> = try {
        val response = api.searchPlaylists(keyword, page, pageSize)
        if (response.code == 200 && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message ?: "歌单搜索失败"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(RepositoryErrorParser.parse(e)))
    }
}
