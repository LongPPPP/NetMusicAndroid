package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SearchSongItem
import com.example.netmusicandroid.data.model.SingerItem
import com.example.netmusicandroid.data.model.UserPlaylist
import com.example.netmusicandroid.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository.getInstance()

    // ── 分类状态（0=歌曲, 1=歌手, 2=歌单） ────────

    private val _selectedCategory = MutableLiveData(0)
    val selectedCategory: LiveData<Int> = _selectedCategory

    // ── 搜索结果 ────────────────────────────────

    private val _songResults = MutableLiveData<List<SearchSongItem>>(emptyList())
    val songResults: LiveData<List<SearchSongItem>> = _songResults

    private val _singerResults = MutableLiveData<List<SingerItem>>(emptyList())
    val singerResults: LiveData<List<SingerItem>> = _singerResults

    private val _playlistResults = MutableLiveData<List<UserPlaylist>>(emptyList())
    val playlistResults: LiveData<List<UserPlaylist>> = _playlistResults

    // ── 状态 ────────────────────────────────────

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    fun clearToast() {
        _toastMsg.postValue("")
    }

    // ── 防抖 ────────────────────────────────────

    private var searchJob: Job? = null
    private var lastKeyword: String = ""

    // ── 分类切换 ────────────────────────────────

    fun selectCategory(category: Int) {
        if (category !in 0..2) return
        _selectedCategory.value = category
    }

    // ── 搜索 ────────────────────────────────────

    fun search(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) {
            _toastMsg.postValue("请输入搜索关键词")
            return
        }
        // 关键词不变且已有结果 → 跳过重复请求
        if (kw == lastKeyword && hasResults()) return
        lastKeyword = kw

        // 取消上一次未完成的搜索，防止并发重复请求
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                when (_selectedCategory.value) {
                    0 -> searchSongs(kw)
                    1 -> searchSingers(kw)
                    2 -> searchPlaylists(kw)
                }
            } catch (e: Exception) {
                _toastMsg.postValue(e.message ?: "搜索失败")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /** 当前分类是否已有结果（用于防止重复请求） */
    private fun hasResults(): Boolean = when (_selectedCategory.value) {
        0 -> !_songResults.value.isNullOrEmpty()
        1 -> !_singerResults.value.isNullOrEmpty()
        2 -> !_playlistResults.value.isNullOrEmpty()
        else -> false
    }

    private suspend fun searchSongs(keyword: String) {
        repository.searchSongs(keyword)
            .onSuccess { _songResults.postValue(it.list) }
            .onFailure { _toastMsg.postValue(it.message ?: "歌曲搜索失败") }
    }

    private suspend fun searchSingers(keyword: String) {
        repository.searchSingers(keyword)
            .onSuccess { _singerResults.postValue(it.list) }
            .onFailure { _toastMsg.postValue(it.message ?: "歌手搜索失败") }
    }

    private suspend fun searchPlaylists(keyword: String) {
        repository.searchPlaylists(keyword)
            .onSuccess { _playlistResults.postValue(it.list) }
            .onFailure { _toastMsg.postValue(it.message ?: "歌单搜索失败") }
    }
}
