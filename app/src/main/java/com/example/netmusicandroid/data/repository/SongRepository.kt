package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.api.CommentRequest
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.data.model.CommentListData
import com.example.netmusicandroid.data.model.SongDetail

import org.json.JSONObject
import retrofit2.HttpException

/**
 * 歌曲数据仓库。
 */
class SongRepository {

    private val api = ApiClient.createService<SongApiService>()

    // 统一处理错误解析
    private fun parseError(e: Throwable): String {
        return if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                e.message() ?: "请求错误"
            }
        } else {
            e.message ?: "网络异常"
        }
    }

    /** 获取歌曲列表 */
    suspend fun fetchSongs(page: Int = 1): Result<List<SongDetail>> = try {
        val response = api.getSongs(page)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "请求失败"))
        } else {
            Result.success(response.data?.list ?: emptyList())
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 获取歌曲详情 */
    suspend fun fetchSongDetail(songId: Int): Result<SongDetail> = try {
        val response = api.getSongDetail(songId)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "请求失败"))
        } else {
            Result.success(response.data ?: throw Exception("歌曲不存在"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 分页获取歌曲评论 */
    suspend fun fetchComments(songId: Int, page: Int = 1): Result<CommentListData> = try {
        val response = api.getComments(songId, page)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "获取评论失败"))
        } else {
            Result.success(response.data ?: throw Exception("暂无数据"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 发表评论 */
    suspend fun addComment(songId: Int, content: String): Result<CommentItem> = try {
        val response = api.postComment(songId, CommentRequest(content))
        if (response.code != 201) {
            Result.failure(Exception(response.message ?: "发表评论失败"))
        } else {
            Result.success(response.data ?: throw Exception("发表成功但未返回数据"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 删除评论 */
    suspend fun removeComment(songId: Int, commentId: Int): Result<Unit> = try {
        val response = api.deleteComment(songId, commentId)
        if (response.code != 200) {
            Result.failure(Exception(response.message ?: "删除失败"))
        } else {
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 构建可播放的完整 URL */
    fun buildPlayUrl(playUrl: String, baseHost: String = "http://10.0.2.2:3000"): String {
        return if (playUrl.startsWith("http")) {
            playUrl   // 已经是完整 URL
        } else {
            "$baseHost$playUrl"   // 相对路径 → 补全
        }
    }
}
