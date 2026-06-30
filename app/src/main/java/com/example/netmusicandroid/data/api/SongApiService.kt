package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.data.model.CommentListData
import com.example.netmusicandroid.data.model.FavoriteData
import com.example.netmusicandroid.data.model.MyCommentListData
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.model.SongListData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

// 发表评论的请求体
data class CommentRequest(
    val content: String
)

interface SongApiService {

    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("singer_id") singerId: Int? = null
    ): ApiResponse<SongListData>

    @GET("songs/{songId}")
    suspend fun getSongDetail(
        @Path("songId") songId: Int
    ): ApiResponse<SongDetail>

    // 【新增】上架歌曲 (需 ARTIST 角色 Token)
    @Multipart
    @POST("songs")
    suspend fun uploadSong(
        @Part("name") name: RequestBody,
        @Part cover: MultipartBody.Part?,
        @Part song: MultipartBody.Part
    ): ApiResponse<SongDetail>

    // 【新增】下架歌曲 (仅限本人)
    @DELETE("songs/{songId}")
    suspend fun deleteSong(
        @Path("songId") songId: Int
    ): ApiResponse<Unit>

    @GET("songs/{songId}/comments")
    suspend fun getComments(
        @Path("songId") songId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<CommentListData>

    @POST("songs/{songId}/comments")
    suspend fun postComment(
        @Path("songId") songId: Int,
        @Body body: CommentRequest
    ): ApiResponse<CommentItem>

    @DELETE("songs/{songId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("songId") songId: Int,
        @Path("commentId") commentId: Int
    ): ApiResponse<Unit>

    /** 我的评论列表 GET /users/me/comments */
    @GET("users/me/comments")
    suspend fun getMyComments(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<MyCommentListData>

    /** 我的收藏 GET /users/me/favorites */
    @GET("users/me/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<FavoriteData>

    /** 我发表的歌曲 GET /users/me/songs */
    @GET("users/me/songs")
    suspend fun getMySongs(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<SongListData>
}
