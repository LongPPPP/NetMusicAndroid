package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.SongRepository
import kotlinx.coroutines.launch

class MySongsViewModel : ViewModel() {

    private val repository = SongRepository.getInstance()

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
                val result = repository.fetchMySongs()
                result.onSuccess { songs ->
                    _songs.postValue(songs)
                }.onFailure { e ->
                    _toastMsg.postValue(e.message ?: "加载失败")
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