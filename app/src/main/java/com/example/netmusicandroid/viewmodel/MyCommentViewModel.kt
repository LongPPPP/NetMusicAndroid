package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.MyCommentItem
import com.example.netmusicandroid.data.repository.SongRepository
import kotlinx.coroutines.launch

class MyCommentViewModel : ViewModel() {

    private val repository = SongRepository.getInstance()

    private val _comments = MutableLiveData<List<MyCommentItem>>(emptyList())
    val comments: LiveData<List<MyCommentItem>> = _comments

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    private var currentPage = 1
    private var hasMore = true

    fun loadFirstPage() {
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.fetchMyComments(1)
                result.onSuccess { data ->
                    _comments.postValue(data.list)
                    hasMore = data.list.size >= data.page_size
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

    fun loadNextPage() {
        if (!hasMore) return
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.fetchMyComments(currentPage + 1)
                result.onSuccess { data ->
                    val merged = _comments.value.orEmpty() + data.list
                    _comments.postValue(merged)
                    currentPage = data.page
                    hasMore = data.list.size >= data.page_size
                }.onFailure { e ->
                    _toastMsg.postValue(e.message ?: "网络异常")
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