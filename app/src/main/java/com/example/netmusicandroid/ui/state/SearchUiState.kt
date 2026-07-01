package com.example.netmusicandroid.ui.state

import com.example.netmusicandroid.data.model.SearchSongItem
import com.example.netmusicandroid.data.model.SingerItem
import com.example.netmusicandroid.data.model.UserPlaylist

data class SearchUiState(
    val selectedCategory: Int = 0,
    val isLoading: Boolean = false,
    val songResults: List<SearchSongItem> = emptyList(),
    val singerResults: List<SingerItem> = emptyList(),
    val playlistResults: List<UserPlaylist> = emptyList()
)
