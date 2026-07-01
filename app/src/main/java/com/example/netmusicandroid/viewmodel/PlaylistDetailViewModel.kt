package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.PlaylistDetailRepository
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.state.PlaylistDetailUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel : ViewModel() {
    private val playlistRepo = PlaylistDetailRepository.getInstance()

    private val _uiState = MutableLiveData(PlaylistDetailUiState())
    val uiState: LiveData<PlaylistDetailUiState> = _uiState

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    /**
     * 加载歌单详情
     */
    fun loadPlaylistDetail(playlistId: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val result = playlistRepo.getPlaylistDetail(playlistId)
            result.onSuccess { detailData ->
                updateState { copy(isLoading = false, playlistDetail = detailData) }
            }.onFailure { error ->
                updateState { copy(isLoading = false) }
                sendToast(error.message ?: "加载歌单失败")
            }
        }
    }

    // 删除歌单内指定歌曲
    fun deleteSongInPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val result = playlistRepo.deleteSongFromPlaylist(playlistId, songId)
            result.onSuccess {
                loadPlaylistDetail(playlistId)
            }.onFailure { err ->
                updateState { copy(isLoading = false) }
                sendToast(err.message ?: "删除歌曲失败")
            }
        }
    }

    // 播放全部歌曲（预留）
    fun playAllSong() {
        viewModelScope.launch { sendToast("开始播放全部歌曲") }
    }

    // 批量操作入口（预留）
    fun openBatchOperate() {
        viewModelScope.launch { sendToast("批量操作功能开发中") }
    }

    private fun updateState(reducer: PlaylistDetailUiState.() -> PlaylistDetailUiState) {
        _uiState.value = (_uiState.value ?: PlaylistDetailUiState()).reducer()
    }

    private suspend fun sendToast(message: String) {
        _events.send(UiEvent.Toast(message))
    }
}
