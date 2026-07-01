package com.example.netmusicandroid.ui.state

import com.example.netmusicandroid.data.model.SongDetail

data class HomeUiState(
    val isLoadingSongs: Boolean = false,
    val songs: List<SongDetail> = emptyList(),
    val songError: String? = null
)
