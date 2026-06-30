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
    private val _currentSong = MutableLiveData<SongDetail?>()
    val currentSong: LiveData<SongDetail?> = _currentSong

    /**
     * 设置当前播放歌曲。
     * 同时自动同步到 Room 播放队列（由队友的功能实现）。
     */
    fun playSong(song: SongDetail?) {
        _currentSong.value = song
        if (song != null) {
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
        }
    }
}
