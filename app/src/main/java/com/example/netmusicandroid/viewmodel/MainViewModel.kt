package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.netmusicandroid.data.model.SongDetail

class MainViewModel : ViewModel() {

    // 当前正在播放的歌曲信息
    private val _currentSong = MutableLiveData<SongDetail?>()
    val currentSong: LiveData<SongDetail?> = _currentSong

    // 播放状态：true 表示正在播放，false 表示暂停
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // 设置当前播放歌曲（首页点击时调用）
    fun playSong(song: SongDetail) {
        _currentSong.value = song
        _isPlaying.value = true // 默认点歌即播放
    }

    // 切换播放/暂停状态
    fun togglePlayState() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }
}
