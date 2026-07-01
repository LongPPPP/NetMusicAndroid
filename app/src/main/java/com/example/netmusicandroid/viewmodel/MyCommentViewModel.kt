package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.SongApiService
import com.example.netmusicandroid.data.model.MyCommentItem
import kotlinx.coroutines.launch

class MyCommentViewModel : ViewModel() {

    private val api = ApiClient.createService<SongApiService>()

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
                val resp = api.getMyComments(1)
                if (resp.code == 200 && resp.data != null) {
                    _comments.postValue(resp.data.list)
                    hasMore = resp.data.list.size >= resp.data.page_size
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

    fun loadNextPage() {
        if (!hasMore) return
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val resp = api.getMyComments(currentPage + 1)
                if (resp.code == 200 && resp.data != null) {
                    val merged = _comments.value.orEmpty() + resp.data.list
                    _comments.postValue(merged)
                    currentPage = resp.data.page
                    hasMore = resp.data.list.size >= resp.data.page_size
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