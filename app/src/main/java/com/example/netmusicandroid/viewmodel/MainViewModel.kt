package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import com.example.netmusicandroid.data.repository.RecentPlayRepository
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

                // 立即写入最近播放（不等待播放完成，用户点击即记录）
                RecentPlayRepository().record(
                    songId = song.song_id,
                    songName = song.song_name,
                    singerName = song.singer_name,
                    playUrl = song.play_url,
                    coverUrl = song.cover_url,
                    duration = song.duration
                )
            }
        } else {
            _isPlaying.value = false
        }
    }

    // 切换播放/暂停状态
    fun togglePlayState() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }
}
