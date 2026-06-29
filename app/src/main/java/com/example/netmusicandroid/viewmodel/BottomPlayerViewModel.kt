package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import com.example.netmusicandroid.data.repository.RecentPlayRepository
import com.example.netmusicandroid.utils.MusicPlayerManager
import kotlinx.coroutines.launch

/**
 * 全局底部播放栏 ViewModel。
 *
 * 桥接 Room 播放队列（PlayQueueRepository）与 MediaPlayer 控制器，
 * 供 BaseActivity 及各独立 Activity 共用。
 */
class BottomPlayerViewModel : ViewModel() {

    private val queueRepo = PlayQueueRepository(AppDatabase.globalPlayQueueDao)
    private val recentRepo = RecentPlayRepository(AppDatabase.globalRecentPlayDao)

    // ── 歌曲信息 ────────────────────────────────

    private val _songName = MutableLiveData("")
    val songName: LiveData<String> = _songName

    private val _singerName = MutableLiveData("")
    val singerName: LiveData<String> = _singerName

    private val _coverUrl = MutableLiveData<String?>()
    val coverUrl: LiveData<String?> = _coverUrl

    // ── 播放状态 ────────────────────────────────

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    /** 队列中是否有当前歌曲（控制底部播放栏可见性） */
    private val _hasCurrentSong = MutableLiveData(false)
    val hasCurrentSong: LiveData<Boolean> = _hasCurrentSong

    // ── Toast ───────────────────────────────────

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    // ── 初始化 ──────────────────────────────────

    init {
        // 1. 观察队列当前歌曲变化 → 更新歌曲信息 LiveData
        viewModelScope.launch {
            queueRepo.observeCurrentSong().collect { entity ->
                _hasCurrentSong.postValue(entity != null)
                _songName.postValue(entity?.song_name ?: "")
                _singerName.postValue(entity?.singer_name ?: "")
                _coverUrl.postValue(entity?.cover_url)
            }
        }

        // 2. 播放错误回调 → Toast 通知用户
        MusicPlayerManager.onError = { _, _ ->
            _toastMsg.postValue("音频加载失败，请检查网络或文件是否存在")
        }

        // 3. 播放完成回调：记录历史 + 自动切下一首
        MusicPlayerManager.onCompletion = {
            viewModelScope.launch {
                recordAndAdvance()
            }
        }
    }

    // ── 播放控制 ────────────────────────────────

    /** 播放 / 暂停切换 */
    fun togglePlayPause() {
        val playing = MusicPlayerManager.toggle()
        _isPlaying.postValue(playing)
    }

    /** 播放下一首 */
    fun playNext() {
        viewModelScope.launch {
            val next = queueRepo.next()
            if (next != null) {
                playEntity(next)
            } else {
                _toastMsg.postValue("已是最后一首")
            }
        }
    }

    /** 播放上一首 */
    fun playPrev() {
        viewModelScope.launch {
            val prev = queueRepo.prev()
            if (prev != null) {
                playEntity(prev)
            } else {
                _toastMsg.postValue("已是第一首")
            }
        }
    }

    /** 同步播放状态（onResume 时调用，防止 MediaPlayer 状态不一致） */
    fun syncPlayState() {
        _isPlaying.postValue(MusicPlayerManager.isPlaying())
    }

    /** 重置 Toast（防止重复弹窗） */
    fun clearToast() {
        _toastMsg.postValue("")
    }

    // ── 内部方法 ────────────────────────────────

    /** 播放入队实体（自动补全相对路径为完整 HTTP URL） */
    private fun playEntity(entity: com.example.netmusicandroid.data.db.PlayQueueEntity) {
        val url = MusicPlayerManager.resolveUrl(entity.play_url)
        if (url == null) {
            _toastMsg.postValue("无法播放：资源地址为空")
            return
        }
        MusicPlayerManager.play(url)
        _isPlaying.postValue(true)
    }

    /** 播放完成时：记录最近播放 + 自动播放下一首 */
    private suspend fun recordAndAdvance() {
        // 记录最近播放历史
        val current = queueRepo.getCurrentSong()
        if (current != null) {
            recentRepo.record(
                songId = current.song_id,
                songName = current.song_name,
                singerName = current.singer_name,
                playUrl = current.play_url,
                coverUrl = current.cover_url,
                duration = current.duration
            )
        }

        // 自动切下一首
        val next = queueRepo.next()
        if (next != null) {
            val url = next.play_url
            if (!url.isNullOrEmpty()) {
                MusicPlayerManager.play(url)
                _isPlaying.postValue(true)
            }
        } else {
            _isPlaying.postValue(false)
        }
    }
}
