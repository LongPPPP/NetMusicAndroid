package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.PlaylistApiService
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.bean.PlaylistData
import com.example.netmusicandroid.data.model.bean.createUserPlaylistReq
import com.example.netmusicandroid.data.model.bean.createUserPlaylistResp
import com.example.netmusicandroid.data.model.bean.UserBean
import com.example.netmusicandroid.data.model.bean.UserPlaylistBean
import retrofit2.Response

class PlaylistRepository {
    private val api = ApiClient.createService<PlaylistApiService>()

    suspend fun getUserInfo(): Response<ApiResponse<UserBean>> {
        return api.getCurrentUserInfo()
    }

    // 获取用户全部自建歌单
    suspend fun getUserPlaylist(userId: Int): Response<ApiResponse<PlaylistData<UserPlaylistBean>>> {
        return api.getUserPlaylist(userId)
    }

    // 删除指定歌单
    suspend fun deleteUserPlaylist(collectionId: Int): Response<ApiResponse<Any>> {
        return api.deleteUserPlaylist(collectionId)
    }

    // 创建新歌单 POST
    suspend fun createUserPlaylist(name: String): ApiResponse<createUserPlaylistResp>? {
        val body = createUserPlaylistReq(name)
        val response = api.createUserPlaylist(body)
        return if (response.isSuccessful) response.body() else null
    }
}