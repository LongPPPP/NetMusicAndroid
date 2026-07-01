package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.data.repository.SongRepository
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {

    private val repository = SongRepository.getInstance()

    // 评论列表数据
    private val _comments = MutableLiveData<List<CommentItem>>(emptyList())
    val comments: LiveData<List<CommentItem>> = _comments

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 分页相关变量
    private var currentPage = 1
    private var totalCount = 0
    private var isLastPage = false

    // 获取评论列表
    fun loadComments(songId: Int, isRefresh: Boolean = false) {
        if (_isLoading.value == true) return
        
        if (isRefresh) {
            currentPage = 1
            isLastPage = false
        } else if (isLastPage) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.fetchComments(songId, currentPage)
            result.onSuccess { data ->
                val currentList = if (isRefresh) emptyList() else _comments.value ?: emptyList()
                _comments.value = currentList + data.list
                
                totalCount = data.total
                currentPage++
                
                // 判断是否已经加载完所有数据
                if (_comments.value?.size ?: 0 >= totalCount) {
                    isLastPage = true
                }
                _error.value = null
            }.onFailure { ex ->
                _error.value = ex.message ?: "加载失败"
            }
            _isLoading.value = false
        }
    }

    // 发表评论
    fun sendComment(songId: Int, content: String, onResult: (Boolean, String) -> Unit) {
        if (content.isBlank()) {
            onResult(false, "评论内容不能为空")
            return
        }

        viewModelScope.launch {
            val result = repository.addComment(songId, content)
            result.onSuccess { newItem ->
                // 发表成功后，将新评论插入到列表开头（模拟实时刷新）
                val currentList = _comments.value?.toMutableList() ?: mutableListOf()
                currentList.add(0, newItem)
                _comments.value = currentList
                onResult(true, "发表成功")
            }.onFailure { ex ->
                onResult(false, ex.message ?: "发表失败")
            }
        }
    }

    // 删除评论
    fun deleteComment(songId: Int, commentId: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.removeComment(songId, commentId)
            result.onSuccess {
                // 删除成功后，从本地列表中移除
                val currentList = _comments.value?.toMutableList() ?: mutableListOf()
                val index = currentList.indexOfFirst { it.comment_id == commentId }
                if (index != -1) {
                    currentList.removeAt(index)
                    _comments.value = currentList
                }
                onResult(true, "删除成功")
            }.onFailure { ex ->
                onResult(false, ex.message ?: "删除失败")
            }
        }
    }
}
