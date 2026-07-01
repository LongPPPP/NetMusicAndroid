package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.SongDetail
import kotlinx.coroutines.launch

class MySongsViewModel : ViewModel() {

    private val api = ApiClient.createService<SongApiService>()

    private val _songs = MutableLiveData<List<SongDetail>>(emptyList())
    val songs: LiveData<List<SongDetail>> = _songs

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    fun loadMySongs() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val resp = api.getMySongs()
                if (resp.code == 200 && resp.data != null) {
                    _songs.postValue(resp.data.list)
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