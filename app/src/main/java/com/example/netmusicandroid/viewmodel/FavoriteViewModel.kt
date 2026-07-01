package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.PlaylistRepository
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.state.FavoritesUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val songRepo = SongRepository.getInstance()
    private val playlistRepo = PlaylistRepository.getInstance()

    // 收藏歌单 ID，加载后缓存，供删除时使用
    private var favoritePlaylistId: Int = -1

    private val _uiState = MutableLiveData(FavoritesUiState())
    val uiState: LiveData<FavoritesUiState> = _uiState

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                val result = songRepo.fetchFavorites()
                result.onSuccess { data ->
                    favoritePlaylistId = data.playlist_id
                    updateState {
                        copy(
                            isLoading = false,
                            playlistName = data.playlist_name,
                            songs = data.songs
                        )
                    }
                }.onFailure { e ->
                    updateState { copy(isLoading = false) }
                    sendToast(e.message ?: "加载失败")
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                sendToast(e.message ?: "网络异常")
            }
        }
    }

    /** 从收藏歌单中移除歌曲 */
    fun removeFavorite(songId: Int) {
        if (favoritePlaylistId == -1) {
            viewModelScope.launch { sendToast("收藏歌单信息缺失，请刷新页面") }
            return
        }
        viewModelScope.launch {
            val result = playlistRepo.removeFavorite(favoritePlaylistId, songId)
            result.onSuccess {
                updateState { copy(songs = songs.filter { it.song_id != songId }) }
                sendToast("已取消收藏")
            }.onFailure { e ->
                sendToast(e.message ?: "取消收藏失败")
            }
        }
    }

    private fun updateState(reducer: FavoritesUiState.() -> FavoritesUiState) {
        _uiState.value = (_uiState.value ?: FavoritesUiState()).reducer()
    }

    private suspend fun sendToast(message: String) {
        _events.send(UiEvent.Toast(message))
    }
}
