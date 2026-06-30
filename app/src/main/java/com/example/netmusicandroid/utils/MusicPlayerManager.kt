package com.example.netmusicandroid.utils

import android.media.MediaPlayer
import android.util.Log
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.SongDetail

/**
 * 音乐播放管理单例
 * 合并逻辑：支持准备状态管理、ID追踪、跨页面歌曲信息同步、路径自动补全
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPreparedFlag = false
    private var currentSongId: Int = -1

    // 全局状态：当前播放详情与首页首选歌曲
    var currentSong: SongDetail? = null
    var firstSongInHome: SongDetail? = null

    var onPrepared: ((Int) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null
    var onError: ((Int, Int) -> Unit)? = null

    /**
     * 将相对路径补全为完整 HTTP 地址。
     */
    fun resolveUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path

        val base = ApiConst.BASE_URL.replace("/api/v1/", "").trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"

        // 核心修复：使用系统的 Uri.encode 进行转义
        val encodedPath = android.net.Uri.encode(cleanPath, "/")

        return "$base$encodedPath"
    }

    /**
     * 播放歌曲
     */
    fun play(url: String, songId: Int = -1): Boolean {
        Log.d("MusicPlayerDebug", "请求播放 URL: $url (ID: $songId)")

        if (url == currentUrl && mediaPlayer != null) {
            Log.d("MusicPlayerDebug", "检测到相同 URL，尝试续播...")
            val mp = mediaPlayer!!
            return try {
                if (isPreparedFlag && !mp.isPlaying) {
                    mp.start()
                    onPrepared?.invoke(mp.duration)
                }
                false
            } catch (e: Exception) {
                releaseQuietly()
                false
            }
        }

        releaseQuietly()
        currentUrl = url
        currentSongId = songId

        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    Log.d("MusicPlayerDebug", "播放器就绪 (Prepared)")
                    isPreparedFlag = true
                    mp.start()
                    onPrepared?.invoke(mp.duration)
                }
                setOnCompletionListener {
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicPlayerDebug", "MediaPlayer 报错: what=$what, extra=$extra, url=$url")
                    isPreparedFlag = false
                    currentUrl = null
                    onError?.invoke(what, extra)
                    releaseQuietly()
                    true
                }
                prepareAsync()
            }
            true
        } catch (e: Exception) {
            Log.e("MusicPlayerDebug", "播放启动失败: ${e.message}")
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
                return false
            } else {
                it.start()
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
    }

    fun getCurrentSongId() = currentSongId

    fun seekTo(msec: Int) {
        if (isPreparedFlag) mediaPlayer?.seekTo(msec)
    }

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