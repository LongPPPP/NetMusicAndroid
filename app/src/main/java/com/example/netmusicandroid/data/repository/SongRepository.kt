package com.example.netmusicandroid.data.repository

import android.content.Context
import android.net.Uri
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.api.CommentRequest
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.data.model.CommentListData
import com.example.netmusicandroid.data.model.SongDetail
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream

class SongRepository {

    private val api = ApiClient.createService<SongApiService>()

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

    /** 上架歌曲 */
    suspend fun publishSong(
        name: String,
        coverFile: File?,
        songFile: File
    ): Result<SongDetail> = try {
        // 使用 plain 类型发送字符串
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        
        // 【精准修复】：明确指定 mimeType 为后端支持的类型
        val songRequestBody = songFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        val songPart = MultipartBody.Part.createFormData("song", songFile.name, songRequestBody)
        
        val coverPart = coverFile?.let {
            // 这里不再使用 image/*，而是改为 image/jpeg
            val coverRequestBody = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("cover", it.name, coverRequestBody)
        }

        val response = api.uploadSong(nameBody, coverPart, songPart)
        if (response.code == 201) {
            Result.success(response.data!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    /** 下架歌曲 */
    suspend fun removeSong(songId: Int): Result<Unit> = try {
        val response = api.deleteSong(songId)
        if (response.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(Exception(parseError(e)))
    }

    fun uriToFile(context: Context, uri: Uri, fileName: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    suspend fun fetchSongs(page: Int = 1): Result<List<SongDetail>> = try {
        val response = api.getSongs(page)
        if (response.code != 200) Result.failure(Exception(response.message))
        else Result.success(response.data?.list ?: emptyList())
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }

    suspend fun fetchSongDetail(songId: Int): Result<SongDetail> = try {
        val response = api.getSongDetail(songId)
        if (response.code != 200) Result.failure(Exception(response.message))
        else Result.success(response.data!!)
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }

    suspend fun fetchComments(songId: Int, page: Int = 1): Result<CommentListData> = try {
        val response = api.getComments(songId, page)
        if (response.code != 200) Result.failure(Exception(response.message))
        else Result.success(response.data!!)
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }

    suspend fun addComment(songId: Int, content: String): Result<CommentItem> = try {
        val response = api.postComment(songId, CommentRequest(content))
        if (response.code != 201) Result.failure(Exception(response.message))
        else Result.success(response.data!!)
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }

    suspend fun removeComment(songId: Int, commentId: Int): Result<Unit> = try {
        val response = api.deleteComment(songId, commentId)
        if (response.code != 200) Result.failure(Exception(response.message))
        else Result.success(Unit)
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }

    suspend fun fetchFavorites(): Result<com.example.netmusicandroid.data.model.FavoriteData> = try {
        val response = api.getFavorites()
        if (response.code != 200) Result.failure(Exception(response.message))
        else Result.success(response.data!!)
    } catch (e: Exception) { Result.failure(Exception(parseError(e))) }
}
