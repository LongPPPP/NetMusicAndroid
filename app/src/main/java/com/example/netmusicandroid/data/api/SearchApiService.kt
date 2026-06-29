package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.PlaylistSearchListData
import com.example.netmusicandroid.data.model.SearchSongListData
import com.example.netmusicandroid.data.model.SingerListData
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 搜索接口（歌曲 / 歌手 / 歌单）。
 * 后端统一入参 keyword（必填）、page（默认 1）、page_size（默认 20）。
 */
interface SearchApiService {

    /** 歌曲搜索：按歌曲名 / 歌手名模糊匹配 */
    @GET("search/songs")
    suspend fun searchSongs(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<SearchSongListData>

    /** 歌手搜索：仅匹配歌手名 */
    @GET("search/singers")
    suspend fun searchSingers(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<SingerListData>

    /** 歌单搜索：仅匹配歌单名 */
    @GET("search/playlists")
    suspend fun searchPlaylists(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PlaylistSearchListData>
}
