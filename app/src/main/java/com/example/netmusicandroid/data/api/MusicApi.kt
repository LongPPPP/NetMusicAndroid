package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.bean.CollectionData
import com.example.netmusicandroid.data.model.bean.CreateCollectionReq
import com.example.netmusicandroid.data.model.bean.CreateCollectionResp
import com.example.netmusicandroid.data.model.bean.UserBean
import com.example.netmusicandroid.data.model.bean.UserCollectionBean
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 后端API接口定义
 * 统一基础路径前缀：/api/v1
 */
interface MusicApi {

    /**
     * GET /api/v1/users/me
     * 获取当前登录用户信息，需要Bearer Token鉴权
     */
    @GET("users/me")

    suspend fun getCurrentUserInfo(): Response<ApiResponse<UserBean>>

    /**
     * GET /api/v1/user/collections
     * 获取用户所有自建歌单
     */
    @GET("users/{userId}/playlists")
    suspend fun getUserCollection(
        @Path("userId") userId: Int
    ): Response<ApiResponse<CollectionData<UserCollectionBean>>>

    /**
     * DELETE /api/v1/playlists/{collectionId}
     * 删除指定歌单
     * @param collectionId 歌单ID
     */
    @DELETE("playlists/{collectionId}")
    suspend fun deleteCollection(@Path("collectionId") collectionId: Int): Response<ApiResponse<Any>>


    // 3. 创建歌单 POST /api/v1/playlists
    @POST("playlists")
    suspend fun createCollection(
        @Body body: CreateCollectionReq
    ): Response<ApiResponse<CreateCollectionResp>>
}