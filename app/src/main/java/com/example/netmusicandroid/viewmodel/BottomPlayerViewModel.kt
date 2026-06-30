package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import com.example.netmusicandroid.data.repository.RecentPlayRepository
import com.example.netmusicandroid.utils.MusicPlayerManager
import kotlinx.coroutines.launch

/**
 * 全局底部播放栏 ViewModel。
 */
class BottomPlayerViewModel : ViewModel() {

    private val queueRepo = PlayQueueRepository(AppDatabase.globalPlayQueueDao)
    private val recentRepo = RecentPlayRepository(AppDatabase.globalRecentPlayDao)

    private val _songName = MutableLiveData("")
    val songName: LiveData<String> = _songName

    private val _singerName = MutableLiveData("")
    val singerName: LiveData<String> = _singerName

    private val _coverUrl = MutableLiveData<String?>()
    val coverUrl: LiveData<String?> = _coverUrl

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _hasCurrentSong = MutableLiveData(false)
    val hasCurrentSong: LiveData<Boolean> = _hasCurrentSong

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    init {
        // 1. 观察队列变化
        viewModelScope.launch {
            queueRepo.observeCurrentSong().collect { entity ->
                _hasCurrentSong.postValue(entity != null)
                _songName.postValue(entity?.song_name ?: "")
                _singerName.postValue(entity?.singer_name ?: "")
                _coverUrl.postValue(entity?.cover_url)
            }
        }

        // 2. 修复：实时监听播放器的真实物理状态
        // 只要播放器内部响了或停了，这里会毫秒级感应并更新 LiveData
        MusicPlayerManager.onStateChanged = { playing ->
            _isPlaying.postValue(playing)
        }

        MusicPlayerManager.onCompletion = {
            viewModelScope.launch { recordAndAdvance() }
        }
    }

    fun togglePlayPause() {
        // 直接拨动开关，上面的监听器会自动处理 _isPlaying 的更新
        MusicPlayerManager.toggle()
    }

    fun playNext() {
        viewModelScope.launch {
            val next = queueRepo.next()
            if (next != null) playEntity(next) else _toastMsg.postValue("已是最后一首")
        }
    }

    fun playPrev() {
        viewModelScope.launch {
            val prev = queueRepo.prev()
            if (prev != null) playEntity(prev) else _toastMsg.postValue("已是第一首")
        }
    }

    fun syncPlayState() {
        _isPlaying.postValue(MusicPlayerManager.isPlaying())
    }

    fun clearToast() {
        _toastMsg.postValue("")
    }

    private fun playEntity(entity: com.example.netmusicandroid.data.db.PlayQueueEntity) {
        val url = MusicPlayerManager.resolveUrl(entity.play_url)
        if (url == null) {
            _toastMsg.postValue("无法播放：资源地址为空")
            return
        }
        MusicPlayerManager.play(url, entity.song_id)
    }

    private suspend fun recordAndAdvance() {
        val current = queueRepo.getCurrentSong()
        if (current != null) {
            recentRepo.record(current.song_id, current.song_name, current.singer_name, current.play_url, current.cover_url, current.duration)
        }
        val next = queueRepo.next()
        if (next != null) {
            val url = MusicPlayerManager.resolveUrl(next.play_url)
            if (url != null) MusicPlayerManager.play(url, next.song_id)
        }
    }
}
