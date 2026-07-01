package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.PlaylistDetailData
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 歌单详情相关接口
 */
interface PlaylistDetailApiService {
    /**
     * GET playlists/{playlistId}
     * 获取歌单详情+内部歌曲列表
     */
    @GET(ApiConst.PLAYLIST_DETAIL + "{playlistId}")
    suspend fun getPlaylistDetail(
        @Path("playlistId") playlistId: Int
    ): Response<ApiResponse<PlaylistDetailData>>

    /**
     * DELETE 移除歌单内指定歌曲
     * @param playlistId 目标歌单id
     * @param songId 待删除歌曲id
     */
    @DELETE(ApiConst.PLAYLIST_DETAIL + "{playlistId}/songs/{songId}")
    suspend fun deleteSong(
        @Path("playlistId") playlistId: Int,
        @Path("songId") songId: Int
    ): Response<ApiResponse<Any>>
}