package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.data.model.CommentListData
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.model.SongListData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 发表评论的请求体
data class CommentRequest(
    val content: String
)

// 歌曲相关接口
// 所有方法都是 suspend 函数，在协程里调用不会阻塞主线程。
interface SongApiService {

    // 分页获取歌曲列表（可选按歌手筛选）
    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("singer_id") singerId: Int? = null   // 可选：按歌手 ID 筛选
    ): ApiResponse<SongListData>

    // 获取歌曲详情（含 play_url）
    @GET("songs/{songId}")
    suspend fun getSongDetail(
        @Path("songId") songId: Int
    ): ApiResponse<SongDetail>

    // 分页获取歌曲评论列表
    @GET("songs/{songId}/comments")
    suspend fun getComments(
        @Path("songId") songId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<CommentListData>

    // 发表评论
    @POST("songs/{songId}/comments")
    suspend fun postComment(
        @Path("songId") songId: Int,
        @Body body: CommentRequest
    ): ApiResponse<CommentItem>

    //删除评论
    @DELETE("songs/{songId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("songId") songId: Int,
        @Path("commentId") commentId: Int
    ): ApiResponse<Unit>
}