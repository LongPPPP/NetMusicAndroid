package com.example.netmusicandroid.utils

import android.media.MediaPlayer
import android.util.Log
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.SongDetail

/**
 * 音乐播放管理单例
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPreparedFlag = false
    private var currentSongId: Int = -1

    var currentSong: SongDetail? = null
    var firstSongInHome: SongDetail? = null

    // 一开始点击歌曲以后,按钮依旧是play,修复：增加一个状态改变的回调，让各 ViewModel 能自动感应
    var onStateChanged: ((Boolean) -> Unit)? = null
    
    var onPrepared: ((Int) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null
    var onError: ((Int, Int) -> Unit)? = null

    fun resolveUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path
        val base = ApiConst.BASE_URL.replace("/api/v1/", "").trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        val encodedPath = android.net.Uri.encode(cleanPath, "/")
        return "$base$encodedPath"
    }

    fun play(url: String, songId: Int = -1): Boolean {
        if (url == currentUrl && mediaPlayer != null) {
            return false
        }

        releaseQuietly()
        currentUrl = url
        currentSongId = songId

        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    isPreparedFlag = true
                    mp.start()
                    onPrepared?.invoke(mp.duration)
                    // 【通知】：我响了！
                    onStateChanged?.invoke(true)
                }
                setOnCompletionListener {
                    onCompletion?.invoke()
                    // 【通知】：我唱完了（停了）
                    onStateChanged?.invoke(false)
                }
                setOnErrorListener { _, what, extra ->
                    isPreparedFlag = false
                    currentUrl = null
                    onError?.invoke(what, extra)
                    releaseQuietly()
                    // 【通知】：出错了（停了）
                    onStateChanged?.invoke(false)
                    true
                }
                prepareAsync()
            }
            true
        } catch (e: Exception) {
            currentUrl = null
            releaseQuietly()
            false
        }
    }

    fun toggle(): Boolean {
        mediaPlayer?.let {
            if (!isPreparedFlag) return@let false
            if (it.isPlaying) {
                it.pause()
                onStateChanged?.invoke(false) // 【通知】：暂停了
                return false
            } else {
                it.start()
                onStateChanged?.invoke(true) // 【通知】：继续播了
                return true
            }
        }
        return false
    }

    fun isPlaying() = isPreparedFlag && mediaPlayer?.isPlaying == true

    fun stop() {
        releaseQuietly()
        currentUrl = null
        currentSongId = -1
        currentSong = null
        onStateChanged?.invoke(false) // 【通知】：彻底停了
    }

    fun getCurrentSongId() = currentSongId
    fun seekTo(msec: Int) { if (isPreparedFlag) mediaPlayer?.seekTo(msec) }
    fun getCurrentPosition() = if (isPreparedFlag) mediaPlayer?.currentPosition ?: 0 else 0
    fun getDuration() = if (isPreparedFlag) mediaPlayer?.duration ?: 0 else 0

    private fun releaseQuietly() {
        isPreparedFlag = false
        mediaPlayer?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            try { it.reset() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
        mediaPlayer = null
    }
}
