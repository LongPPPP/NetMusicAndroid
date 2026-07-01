package com.example.netmusicandroid.utils

import android.media.MediaPlayer
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.SongDetail

/**
 * 音乐播放管理单例
 * 改造说明：由「单回调覆盖」改为「多监听器列表」模式
 * 支持多个 ViewModel 同时监听播放状态，后打开的页面不会覆盖之前的监听
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPreparedFlag = false
    private var currentSongId: Int = -1

    var currentSong: SongDetail? = null
    var firstSongInHome: SongDetail? = null

    // ── 多监听器列表（替换原单回调变量，支持多页面同时订阅）────────────────
    // 播放状态变化监听器（播放/暂停时回调）
    private val stateListeners = mutableListOf<(Boolean) -> Unit>()
    // 播放器准备完成监听器（回调时长）
    private val preparedListeners = mutableListOf<(Int) -> Unit>()
    // 播放完成监听器
    private val completionListeners = mutableListOf<() -> Unit>()
    // 播放出错监听器
    private val errorListeners = mutableListOf<(Int, Int) -> Unit>()

    // ── 监听器注册/移除方法 ───────────────────────────────────────────
    /** 注册播放状态监听 */
    fun addOnStateChangedListener(listener: (Boolean) -> Unit) {
        stateListeners.add(listener)
    }
    /** 移除播放状态监听 */
    fun removeOnStateChangedListener(listener: (Boolean) -> Unit) {
        stateListeners.remove(listener)
    }

    /** 注册准备完成监听 */
    fun addOnPreparedListener(listener: (Int) -> Unit) {
        preparedListeners.add(listener)
    }
    /** 移除准备完成监听 */
    fun removeOnPreparedListener(listener: (Int) -> Unit) {
        preparedListeners.remove(listener)
    }

    /** 注册播放完成监听 */
    fun addOnCompletionListener(listener: () -> Unit) {
        completionListeners.add(listener)
    }
    /** 移除播放完成监听 */
    fun removeOnCompletionListener(listener: () -> Unit) {
        completionListeners.remove(listener)
    }

    /** 注册播放出错监听 */
    fun addOnErrorListener(listener: (Int, Int) -> Unit) {
        errorListeners.add(listener)
    }
    /** 移除播放出错监听 */
    fun removeOnErrorListener(listener: (Int, Int) -> Unit) {
        errorListeners.remove(listener)
    }

    // ── 公共方法 ─────────────────────────────────────────────────────
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
                    // 遍历通知所有监听者：准备完成
                    preparedListeners.forEach { it(mp.duration) }
                    // 遍历通知所有监听者：播放状态变为播放中
                    stateListeners.forEach { it(true) }
                }
                setOnCompletionListener {
                    // 遍历通知所有监听者：播放完成
                    completionListeners.forEach { it() }
                    // 遍历通知所有监听者：播放状态变为暂停
                    stateListeners.forEach { it(false) }
                }
                setOnErrorListener { _, what, extra ->
                    isPreparedFlag = false
                    currentUrl = null
                    // 遍历通知所有监听者：播放出错
                    errorListeners.forEach { it(what, extra) }
                    releaseQuietly()
                    // 遍历通知所有监听者：播放状态变为暂停
                    stateListeners.forEach { it(false) }
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
                // 遍历通知所有监听者：已暂停
                stateListeners.forEach { it(false) }
                return false
            } else {
                it.start()
                // 遍历通知所有监听者：已播放
                stateListeners.forEach { it(true) }
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
        // 遍历通知所有监听者：已停止
        stateListeners.forEach { it(false) }
    }

    fun getCurrentSongId() = currentSongId
    fun seekTo(msec: Int) { if (isPreparedFlag) mediaPlayer?.seekTo(msec) }
    fun getCurrentPosition() = if (isPreparedFlag) mediaPlayer?.currentPosition ?: 0 else 0
    fun getDuration() = if (isPreparedFlag) mediaPlayer?.duration ?: 0 else 0

    // ── 内部工具方法 ─────────────────────────────────────────────────
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