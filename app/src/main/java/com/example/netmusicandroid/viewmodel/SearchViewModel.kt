package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.SearchRepository
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.state.SearchUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository.getInstance()

    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    private var searchJob: Job? = null
    private var lastKeyword: String = ""

    fun selectCategory(category: Int) {
        if (category !in 0..2) return
        updateState { copy(selectedCategory = category) }
    }

    fun search(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) {
            viewModelScope.launch { sendToast("请输入搜索关键词") }
            return
        }
        if (kw == lastKeyword && hasResults()) return
        lastKeyword = kw

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                when (currentState().selectedCategory) {
                    0 -> searchSongs(kw)
                    1 -> searchSingers(kw)
                    2 -> searchPlaylists(kw)
                }
            } catch (e: Exception) {
                sendToast(e.message ?: "搜索失败")
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun hasResults(): Boolean = when (currentState().selectedCategory) {
        0 -> currentState().songResults.isNotEmpty()
        1 -> currentState().singerResults.isNotEmpty()
        2 -> currentState().playlistResults.isNotEmpty()
        else -> false
    }

    private suspend fun searchSongs(keyword: String) {
        repository.searchSongs(keyword)
            .onSuccess { data -> updateState { copy(songResults = data.list) } }
            .onFailure { sendToast(it.message ?: "歌曲搜索失败") }
    }

    private suspend fun searchSingers(keyword: String) {
        repository.searchSingers(keyword)
            .onSuccess { data -> updateState { copy(singerResults = data.list) } }
            .onFailure { sendToast(it.message ?: "歌手搜索失败") }
    }

    private suspend fun searchPlaylists(keyword: String) {
        repository.searchPlaylists(keyword)
            .onSuccess { data -> updateState { copy(playlistResults = data.list) } }
            .onFailure { sendToast(it.message ?: "歌单搜索失败") }
    }

    private fun currentState(): SearchUiState = _uiState.value ?: SearchUiState()

    private fun updateState(reducer: SearchUiState.() -> SearchUiState) {
        _uiState.value = currentState().reducer()
    }

    private suspend fun sendToast(message: String) {
        _events.send(UiEvent.Toast(message))
    }
}
