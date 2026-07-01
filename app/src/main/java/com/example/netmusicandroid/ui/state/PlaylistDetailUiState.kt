package com.example.netmusicandroid.ui.state

import com.example.netmusicandroid.data.model.PlaylistDetailData

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val playlistDetail: PlaylistDetailData? = null
)
