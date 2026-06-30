package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<SongDetail?>()
    val currentSong: LiveData<SongDetail?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    fun playSong(song: SongDetail?) {
        _currentSong.value = song
        if (song != null) {
            _isPlaying.value = true
            // 自动同步到 Room 播放队列（队友的功能）
            viewModelScope.launch {
                try {
                    val repo = PlayQueueRepository()
                    val queue = repo.getQueue()
                    val existing = queue.find { it.song_id == song.song_id }
                    if (existing != null) {
                        repo.markAsCurrent(existing.id)
                    } else {
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
                } catch (_: Exception) {}
            }
        } else {
            _isPlaying.value = false
        }
    }

    fun togglePlayState() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }
}