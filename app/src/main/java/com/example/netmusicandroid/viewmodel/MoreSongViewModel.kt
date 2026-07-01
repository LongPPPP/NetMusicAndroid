package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.SongDetail
import kotlinx.coroutines.launch

class MoreSongViewModel : ViewModel() {

    private val api = ApiClient.createService<SongApiService>()

    private val _songs = MutableLiveData<List<SongDetail>>(emptyList())
    val songs: LiveData<List<SongDetail>> = _songs

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    private var currentPage = 0
    private val pageSize = 20

    fun loadFirstPage() {
        currentPage = 0
        _hasMore.postValue(true)
        _songs.postValue(emptyList())
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value == true || _hasMore.value != true) return
        // 先置标记，防止滚动事件在协程启动前重复触发
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val page = currentPage + 1
                val resp = api.getSongs(page = page, pageSize = pageSize)
                if (resp.code == 200 && resp.data != null) {
                    val newList = resp.data.list
                    val merged = (_songs.value ?: emptyList()) + newList
                    _songs.postValue(merged)
                    currentPage = page
                    _hasMore.postValue(newList.size >= pageSize)
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
