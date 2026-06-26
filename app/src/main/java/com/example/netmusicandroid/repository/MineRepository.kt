package com.example.netmusicandroid.repository

import com.example.netmusicandroid.api.ApiClient
import com.example.netmusicandroid.api.ApiResponse
import com.example.netmusicandroid.bean.UserBean
import com.example.netmusicandroid.bean.CollectionData
import com.example.netmusicandroid.bean.UserCollectionBean
import com.example.netmusicandroid.bean.CreateCollectionReq
import com.example.netmusicandroid.bean.CreateCollectionResp
import retrofit2.Response

class MineRepository {
    private val api = ApiClient.musicApi

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