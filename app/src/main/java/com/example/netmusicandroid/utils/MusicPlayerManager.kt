package com.example.netmusicandroid.utils

import android.media.MediaPlayer
import android.util.Log
import com.example.netmusicandroid.constant.ApiConst

/**
 * 音乐播放管理单例
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    // 手动维护准备状态标记，彻底避免PREPARING阶段调用API触发-38错误
    private var isPreparedFlag = false
    private var currentSongId: Int = -1 // 【新增】：记录当前歌曲 ID

    var onPrepared: ((Int) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null
    var onError: ((Int, Int) -> Unit)? = null

    /**
     * 将相对路径补全为完整 HTTP 地址。
     * 后端返回形如 /static/songs/xxx.mp3 的相对路径时，自动拼接 BASE_URL 的协议+主机部分。
     */
    fun resolveUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = ApiConst.BASE_URL.removeSuffix("/api/v1/").removeSuffix("/")
        return "$base${path}".replace(" ", "%20")
    }

    /**
     * 播放歌曲。
     * @return true 开始播新歌，false 同一 URL 已在播放中。
     */
    fun play(url: String, songId: Int = -1): Boolean {
        // 同一URL且播放器存在时，处理续播逻辑
        if (url == currentUrl && mediaPlayer != null) {
            val mp = mediaPlayer!!
            return try {
                // 仅在已完成准备的状态下操作，避免PREPARING阶段非法调用
                if (isPreparedFlag && !mp.isPlaying) {
                    mp.start()
                    onPrepared?.invoke(mp.duration)
                }
                false
            } catch (e: Exception) {
                // 状态异常 → 走完整重建流程
                releaseQuietly()
                false
            }
        }

        // 安全释放旧播放器
        releaseQuietly()
        currentUrl = url
        currentSongId = songId // 【记录 ID】

        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { mp ->
                    // 准备完成后再标记状态、执行播放与回调
                    isPreparedFlag = true
                    mp.start()
                    onPrepared?.invoke(mp.duration)
                }
                setOnCompletionListener {
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("MusicPlayer", "Error what=$what extra=$extra url=$url")
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
            Log.e("MusicPlayer", "play exception: ${e.message}")
            currentUrl = null
            releaseQuietly()
            false
        }
    }

    fun toggle(): Boolean {
        mediaPlayer?.let {
            return try {
                if (!isPreparedFlag) return@let false
                if (it.isPlaying) {
                    it.pause()
                    false
                } else {
                    it.start()
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
        return false
    }

    fun isPlaying(): Boolean {
        return try {
            isPreparedFlag && mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    /** 安全释放（容错：播放器处于 Error/Idle 状态时 stop/release 会抛异常） */
    fun stop() {
        releaseQuietly()
        currentUrl = null
        currentSongId = -1 // 【重置 ID】
    }

    fun getCurrentSongId() = currentSongId // 【暴露获取方法】

    fun seekTo(msec: Int) {
        try {
            if (isPreparedFlag) {
                mediaPlayer?.seekTo(msec)
            }
        } catch (e: Exception) {
            // 未就绪时忽略
        }
    }

    /** 获取当前播放位置。未就绪时返回 0，不抛异常。 */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.takeIf { isPreparedFlag }?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /** 获取总时长。未就绪时返回 0，不抛异常。 */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.takeIf { isPreparedFlag }?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // ── 内部 ────────────────────────────────────

    /** 安全释放，容错各异常状态 */
    private fun releaseQuietly() {
        val mp = mediaPlayer ?: return
        // 释放前先重置状态标记，避免后续调用触发异常
        isPreparedFlag = false
        try {
            if (mp.isPlaying) mp.stop()
        } catch (_: Exception) {}
        try {
            mp.reset()
        } catch (_: Exception) {}
        try {
            mp.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
