package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.SongItem
import com.example.netmusicandroid.data.repository.PlaylistRepository
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val api = ApiClient.createService<SongApiService>()
    private val playlistRepo = PlaylistRepository()

    // 收藏歌单 ID，加载后缓存，供删除时使用
    private var favoritePlaylistId: Int = -1

    private val _songs = MutableLiveData<List<SongItem>>(emptyList())
    val songs: LiveData<List<SongItem>> = _songs

    private val _playlistName = MutableLiveData("我的收藏")
    val playlistName: LiveData<String> = _playlistName

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val resp = api.getFavorites()
                if (resp.code == 200 && resp.data != null) {
                    favoritePlaylistId = resp.data.playlist_id
                    _songs.postValue(resp.data.songs)
                    _playlistName.postValue(resp.data.playlist_name)
                } else {
                    _toastMsg.postValue(resp.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _toastMsg.postValue(e.message ?: "网络异常")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /** 从收藏歌单中移除歌曲 */
    fun removeFavorite(songId: Int) {
        if (favoritePlaylistId == -1) {
            _toastMsg.postValue("收藏歌单信息缺失，请刷新页面")
            return
        }
        viewModelScope.launch {
            val result = playlistRepo.removeFavorite(favoritePlaylistId, songId)
            result.onSuccess {
                // 从本地列表移除该项，UI 即时更新
                val updated = _songs.value?.filter { it.song_id != songId } ?: emptyList()
                _songs.postValue(updated)
                _toastMsg.postValue("已取消收藏")
            }.onFailure { e ->
                _toastMsg.postValue(e.message ?: "取消收藏失败")
            }
        }
    }

    fun clearToast() {
        _toastMsg.postValue("")
    }
}