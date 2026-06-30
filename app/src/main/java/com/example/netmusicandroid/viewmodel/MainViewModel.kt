package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // 当前正在播放的歌曲信息
    private val _currentSong = MutableLiveData<SongDetail?>()//只有MainViewModel能访问
    val currentSong: LiveData<SongDetail?> = _currentSong//给别人看的不可改

    // 播放状态：true 表示正在播放，false 表示暂停
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // 设置当前播放歌曲（首页点击时调用）
    fun playSong(song: SongDetail?) {
        _currentSong.value = song
        if (song != null) {
            _isPlaying.value = true // 点歌即播放

            viewModelScope.launch {
                val repo = PlayQueueRepository()
                val queue = repo.getQueue()
                val existing = queue.find { it.song_id == song.song_id }

                if (existing != null) {
                    // 已在队列中 → 直接标记为当前
                    repo.markAsCurrent(existing.id)
                } else {
                    // 不在队列中 → 追加并标记为当前
                    repo.append(
                        songId = song.song_id,
                        songName = song.song_name,
                        singerName = song.singer_name,
                        playUrl = song.play_url,
                        coverUrl = song.cover_url,
                        duration = song.duration
                    )
                    repo.getQueue().lastOrNull()?.let { repo.markAsCurrent(it.id) }
                }
            }
        } else {
            _isPlaying.value = false // 清空即停止
        }
    }

    // 切换播放/暂停状态
    fun togglePlayState() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }
}
