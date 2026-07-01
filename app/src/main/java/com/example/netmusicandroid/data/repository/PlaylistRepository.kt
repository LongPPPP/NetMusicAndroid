package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.PlaylistApiService
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.UserInfo
import com.example.netmusicandroid.data.model.PlaylistData
import com.example.netmusicandroid.data.model.CreatePlaylistReq
import com.example.netmusicandroid.data.model.CreatePlaylistResp
import com.example.netmusicandroid.data.model.UserPlaylist
import retrofit2.Response

class PlaylistRepository private constructor() {
    private val api = ApiClient.createService<PlaylistApiService>()

    companion object {
        @Volatile
        private var INSTANCE: PlaylistRepository? = null

        fun getInstance(): PlaylistRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaylistRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun getUserInfo(): Response<ApiResponse<UserInfo>> {
        return api.getCurrentUserInfo()
    }

    // 获取用户全部自建歌单
    suspend fun getUserPlaylist(userId: Int): Response<ApiResponse<PlaylistData<UserPlaylist>>> {
        return api.getUserPlaylist(userId)
    }

    // 删除指定歌单
    suspend fun deleteUserPlaylist(collectionId: Int): Response<ApiResponse<Any>> {
        return api.deleteUserPlaylist(collectionId)
    }

    // 创建新歌单 POST
    suspend fun createUserPlaylist(name: String): ApiResponse<CreatePlaylistResp>? {
        val body = CreatePlaylistReq(name)
        val response = api.createUserPlaylist(body)
        return if (response.isSuccessful) response.body() else null
    }

    /**
     * 添加歌曲到指定歌单 POST /playlists/{playlistId}/songs
     * 201 成功 | 400 参数校验失败(有JSON) | 401/403/404/409 仅HTTP状态码(无JSON)
     */
    suspend fun addSongToPlaylist(playlistId: Int, songId: Int): Result<String> = try {
        val body = mapOf("song_id" to songId)
        val response = api.addSongToPlaylist(playlistId, body)
        when (response.code()) {
            201 -> Result.success("已添加到歌单")
            400 -> {
                val msg = response.body()?.message ?: "参数校验失败"
                Result.failure(Exception(msg))
            }
            401 -> Result.failure(Exception("请先登录"))
            403 -> Result.failure(Exception("无歌单操作权限"))
            404 -> Result.failure(Exception("歌单或歌曲不存在"))
            409 -> Result.failure(Exception("歌曲已在歌单内"))
            else -> Result.failure(Exception(response.body()?.message ?: "添加失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 收藏：歌单添加歌曲
    suspend fun addFavorite(playlistId: Int, songId: Int): Result<Unit> = try {
        val body = mapOf("song_id" to songId)
        val response = api.addSongToPlaylist(playlistId, body)
        if (response.isSuccessful && (response.body()?.code == 200 || response.body()?.code == 201)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.body()?.message ?: "添加失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 取消收藏：歌单移除歌曲
    suspend fun removeFavorite(playlistId: Int, songId: Int): Result<Unit> = try {
        val response = api.removeSongFromPlaylist(playlistId, songId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.body()?.message ?: "移除失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}