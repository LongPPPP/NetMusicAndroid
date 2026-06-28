package com.example.netmusicandroid.utils

import android.media.MediaPlayer
import android.util.Log

/**
 * 音乐播放管理单例
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    
    var onPrepared: ((Int) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null

    /**
     * 播放歌曲
     * 返回值：true 表示开始播新歌，false 表示正在播这首，无需动作
     */
    fun play(url: String): Boolean {
        if (url == currentUrl && mediaPlayer != null) {
            return false 
        }

        try {
            stop()
            currentUrl = url
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { 
                    start()
                    onPrepared?.invoke(it.duration)
                }
                setOnCompletionListener {
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    currentUrl = null
                    true
                }
            }
        } catch (e: Exception) {
            currentUrl = null
        }
        return true
    }

    fun toggle(): Boolean {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                return false
            } else {
                it.start()
                return true
            }
        }
        return false
    }

    fun isPlaying() = mediaPlayer?.isPlaying ?: false

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }

    fun getCurrentPosition() = mediaPlayer?.currentPosition ?: 0
    fun getDuration() = mediaPlayer?.duration ?: 0
}
