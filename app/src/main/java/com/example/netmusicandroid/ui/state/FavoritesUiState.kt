package com.example.netmusicandroid.ui.state

import com.example.netmusicandroid.data.model.SongItem

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val playlistName: String = "我的收藏",
    val songs: List<SongItem> = emptyList()
)
