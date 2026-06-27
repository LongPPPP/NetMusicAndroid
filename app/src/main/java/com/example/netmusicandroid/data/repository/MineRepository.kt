package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.MusicApi
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.bean.CollectionData
import com.example.netmusicandroid.data.model.bean.CreateCollectionReq
import com.example.netmusicandroid.data.model.bean.CreateCollectionResp
import com.example.netmusicandroid.data.model.bean.UserBean
import com.example.netmusicandroid.data.model.bean.UserCollectionBean
import retrofit2.Response

class MineRepository {
    private val api = ApiClient.createService<MusicApi>()

    suspend fun getUserInfo(): Response<ApiResponse<UserBean>> {
        return api.getCurrentUserInfo()
    }

    // 获取用户全部自建歌单
    suspend fun getUserCollection(userId: Int): Response<ApiResponse<CollectionData<UserCollectionBean>>> {
        return api.getUserCollection(userId)
    }

    // 删除指定歌单
    suspend fun deleteCollection(collectionId: Int): Response<ApiResponse<Any>> {
        return api.deleteCollection(collectionId)
    }

    // 创建新歌单 POST
    suspend fun createCollection(name: String): ApiResponse<CreateCollectionResp>? {
        val body = CreateCollectionReq(name)
        val response = api.createCollection(body)
        return if (response.isSuccessful) response.body() else null
    }
}