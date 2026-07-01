package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.SongItem
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val api = ApiClient.createService<SongApiService>()

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

    fun clearToast() {
        _toastMsg.postValue("")
    }
}