package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.CreatePlaylistReq
import com.example.netmusicandroid.data.model.CreatePlaylistResp
import com.example.netmusicandroid.data.model.PlaylistData
import com.example.netmusicandroid.data.model.UserInfo
import com.example.netmusicandroid.data.model.UserPlaylist
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
interface PlaylistApiService {

    /**
     * GET /api/v1/users/me
     * 获取当前登录用户信息，需要Bearer Token鉴权
     */
    @GET("users/me")
    suspend fun getCurrentUserInfo(): Response<ApiResponse<UserInfo>>

    /**
     * GET /api/v1/user/collections
     * 获取用户所有自建歌单
     */
    @GET("users/{userId}/playlists")
    suspend fun getUserPlaylist(
        @Path("userId") userId: Int
    ): Response<ApiResponse<PlaylistData<UserPlaylist>>>

    /**
     * DELETE /api/v1/playlists/{collectionId}
     * 删除指定歌单
     * @param collectionId 歌单ID
     */
    @DELETE("playlists/{collectionId}")
    suspend fun deleteUserPlaylist(@Path("collectionId") collectionId: Int): Response<ApiResponse<Any>>

    /**
     * 3. 创建歌单 POST /api/v1/playlists
     */
    @POST("playlists")
    suspend fun createUserPlaylist(
        @Body body: CreatePlaylistReq
    ): Response<ApiResponse<CreatePlaylistResp>>

    /**
     * 歌单添加歌曲 POST /playlists/{playlistId}/songs
     */
    @POST("playlists/{playlistId}/songs")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Int,
        @Body body: Map<String, Int>
    ): Response<ApiResponse<Unit>>

    /**
     * 歌单移除歌曲 DELETE /playlists/{playlistId}/songs/{songId}
     */
    @DELETE("playlists/{playlistId}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Int,
        @Path("songId") songId: Int
    ): Response<ApiResponse<Unit>>

}