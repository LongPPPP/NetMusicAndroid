package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.PlayQueueEntity
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import com.example.netmusicandroid.data.repository.PlaylistRepository
import com.example.netmusicandroid.data.repository.RecentPlayRepository
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.MusicPlayerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 全局播放 ViewModel（原 MainViewModel + BottomPlayerViewModel 合并）
 * 统一管理：播放队列、最近播放、当前歌曲、播放状态、迷你播放栏 UI
 * 切歌支持首尾循环：最后一首下一首 → 第一首；第一首上一首 → 最后一首
 *
 * 改造说明：
 * 1. 仓库改用全局单例，所有 VM 实例共用同一份队列、同一份最近播放数据
 * 2. 播放器回调改为注册监听器模式，支持多页面同时监听，互不覆盖
 * 3. VM 销毁时自动移除监听器，避免内存泄漏
 * 4. 播放按钮增加冷启动容错：播放器未初始化时自动恢复当前歌曲并播放
 */
class BottomPlayerViewModel : ViewModel() {
    // 仓库改用全局单例，多 VM 实例共享同一份数据源
    private val queueRepo = PlayQueueRepository.getInstance()
    private val recentRepo = RecentPlayRepository.getInstance()
    private val playlistRepo = PlaylistRepository()
    private val songRepo = SongRepository()

    private val authRepository = AuthRepository.getInstance()
    val currentUserFlow: Flow<UserEntity?> = authRepository.observeCurrentLoginUser()

    private var favoritePlaylistId: Int = -1

    // ── 迷你播放栏 UI 状态 ──────────────────────
    private val _songName = MutableLiveData("")
    val songName: LiveData<String> = _songName
    private val _singerName = MutableLiveData("")
    val singerName: LiveData<String> = _singerName
    private val _coverUrl = MutableLiveData<String?>()
    val coverUrl: LiveData<String?> = _coverUrl
    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    private val _hasCurrentSong = MutableLiveData(false)
    val hasCurrentSong: LiveData<Boolean> = _hasCurrentSong
    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    // ── 收藏状态 ────────────────────────────────
    private val _isLiked = MutableLiveData(false)
    val isLiked: LiveData<Boolean> = _isLiked

    // ── 全屏播放器 UI 状态 ──────────────────────
    private val _currentSong = MutableLiveData<SongDetail?>()
    val currentSong: LiveData<SongDetail?> = _currentSong

    // 保存监听器引用，用于 VM 销毁时移除，防止内存泄漏
    private val stateListener: (Boolean) -> Unit = { _isPlaying.postValue(it) }
    private val completionListener: () -> Unit = {
        viewModelScope.launch { recordAndAdvance() }
    }

    init {
        viewModelScope.launch {
            queueRepo.observeCurrentSong().collect { entity ->
                _hasCurrentSong.postValue(entity != null)
                _songName.postValue(entity?.song_name ?: "")
                _singerName.postValue(entity?.singer_name ?: "")
                _coverUrl.postValue(entity?.cover_url)
                // 切歌时同步更新全屏播放器数据，保证 PlayerFragment 能收到切歌通知
                _currentSong.postValue(entity?.let {
                    SongDetail(
                        song_id = it.song_id,
                        song_name = it.song_name,
                        singer_name = it.singer_name,
                        play_url = it.play_url,
                        cover_url = it.cover_url,
                        duration = it.duration
                    )
                })
                // 切歌后异步检查收藏状态
                entity?.let { checkIsLiked(it.song_id) }
            }
        }
        // 注册播放器监听器（多监听器模式，不会覆盖其他页面的监听）
        MusicPlayerManager.addOnStateChangedListener(stateListener)
        MusicPlayerManager.addOnCompletionListener(completionListener)
    }

    // ── 播放入口（各页面点击歌曲时调用） ─────────

    /** 播放歌曲：写入队列 + 标记当前 + 记录最近播放 + 实际播放 */
    fun playSong(song: SongDetail?) {
        _currentSong.value = song
        if (song != null) {
            _isPlaying.value = true
            // 触发实际音频播放
            val url = MusicPlayerManager.resolveUrl(song.play_url)
            if (url != null) {
                MusicPlayerManager.play(url, song.song_id)
            }
            viewModelScope.launch {
                val queue = queueRepo.getQueue()
                val existing = queue.find { it.song_id == song.song_id }
                if (existing != null) queueRepo.markAsCurrent(existing.id)
                else {
                    queueRepo.append(
                        song.song_id, song.song_name, song.singer_name,
                        song.play_url, song.cover_url, song.duration
                    )
                    queueRepo.getQueue().lastOrNull()?.let { queueRepo.markAsCurrent(it.id) }
                }
                recentRepo.record(
                    song.song_id, song.song_name, song.singer_name,
                    song.play_url, song.cover_url, song.duration
                )
            }
        } else {
            _isPlaying.value = false
        }
    }

    // ── 播放控制 ────────────────────────────────

    /**
     * 播放/暂停切换
     * 容错逻辑：播放器未初始化时，自动从队列恢复当前歌曲并播放
     */
    fun togglePlayPause() {
        // 播放器已加载过歌曲，直接切换播放/暂停
        if (MusicPlayerManager.getCurrentSongId() != -1) {
            MusicPlayerManager.toggle()
            return
        }

        // 播放器未初始化（冷启动/登录后首次点击），从队列恢复当前歌曲
        viewModelScope.launch {
            val currentSong = queueRepo.getCurrentSong()
            if (currentSong != null) {
                playEntity(currentSong)
            }
        }
    }

    /**
     * 下一首：支持循环播放
     * 当前是最后一首时，提示后自动跳转到第一首
     */
    fun playNext() {
        viewModelScope.launch {
            val nextSong = queueRepo.next()
            if (nextSong != null) {
                playEntity(nextSong)
            } else {
                // 已是最后一首，循环到第一首
                val queue = queueRepo.getQueue()
                if (queue.isNotEmpty()) {
                    _toastMsg.postValue("已是最后一首，将播放第一首")
                    queue.firstOrNull()?.let { first ->
                        queueRepo.markAsCurrent(first.id)
                        playEntity(first)
                    }
                } else {
                    _toastMsg.postValue("播放队列为空")
                }
            }
        }
    }

    /**
     * 上一首：支持循环播放
     * 当前是第一首时，提示后自动跳转到最后一首
     */
    fun playPrev() {
        viewModelScope.launch {
            val prevSong = queueRepo.prev()
            if (prevSong != null) {
                playEntity(prevSong)
            } else {
                // 已是第一首，循环到最后一首
                val queue = queueRepo.getQueue()
                if (queue.isNotEmpty()) {
                    _toastMsg.postValue("已是第一首，将播放最后一首")
                    queue.lastOrNull()?.let { last ->
                        queueRepo.markAsCurrent(last.id)
                        playEntity(last)
                    }
                } else {
                    _toastMsg.postValue("播放队列为空")
                }
            }
        }
    }

    /** 收藏/取消收藏 切换 */
    fun toggleFavorite() {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            // 确保有收藏歌单 ID
            if (favoritePlaylistId == -1) {
                val success = fetchFavoritePlaylistId()
                if (!success) {
                    _toastMsg.postValue("无法获取收藏歌单信息")
                    return@launch
                }
            }

            if (_isLiked.value == true) {
                // 取消收藏
                val res = playlistRepo.removeFavorite(favoritePlaylistId, song.song_id)
                if (res.isSuccess) {
                    _isLiked.postValue(false)
                    _toastMsg.postValue("已取消收藏")
                } else {
                    _toastMsg.postValue("取消收藏失败")
                }
            } else {
                // 添加收藏
                val res = playlistRepo.addFavorite(favoritePlaylistId, song.song_id)
                if (res.isSuccess) {
                    _isLiked.postValue(true)
                    _toastMsg.postValue("收藏成功")
                } else {
                    _toastMsg.postValue("收藏失败")
                }
            }
        }
    }

    private suspend fun fetchFavoritePlaylistId(): Boolean {
        try {
            val res = songRepo.fetchFavorites()
            if (res.isSuccess) {
                favoritePlaylistId = res.getOrNull()?.playlist_id ?: -1
                return favoritePlaylistId != -1
            }
        } catch (e: Exception) {}
        return false
    }

    private fun checkIsLiked(songId: Int) {
        viewModelScope.launch {
            try {
                val res = songRepo.fetchFavorites()
                res.onSuccess { data ->
                    favoritePlaylistId = data.playlist_id
                    val liked = data.songs.any { it.song_id == songId }
                    _isLiked.postValue(liked)
                }.onFailure {
                    _isLiked.postValue(false)
                }
            } catch (e: Exception) {
                _isLiked.postValue(false)
            }
        }
    }

    fun syncPlayState() { _isPlaying.postValue(MusicPlayerManager.isPlaying()) }
    fun clearToast() { _toastMsg.postValue("") }

    // ── 生命周期 ────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // VM 销毁时移除播放器监听器，防止内存泄漏
        MusicPlayerManager.removeOnStateChangedListener(stateListener)
        MusicPlayerManager.removeOnCompletionListener(completionListener)
    }

    // ── 内部方法 ────────────────────────────────

    private fun playEntity(entity: PlayQueueEntity) {
        val url = MusicPlayerManager.resolveUrl(entity.play_url)
            ?: run {
                _toastMsg.postValue("无法播放：资源地址为空")
                return
            }
        // 切歌后立即标记播放状态，UI 无延迟响应
        _isPlaying.postValue(true)
        MusicPlayerManager.play(url, entity.song_id)
    }

    private suspend fun recordAndAdvance() {
        queueRepo.getCurrentSong()?.let {
            recentRepo.record(
                it.song_id, it.song_name, it.singer_name,
                it.play_url, it.cover_url, it.duration
            )
        }
        // 播放完成自动切歌也同步支持循环
        val next = queueRepo.next()
        if (next != null) {
            MusicPlayerManager.resolveUrl(next.play_url)?.run {
                MusicPlayerManager.play(this, next.song_id)
            }
        } else {
            val queue = queueRepo.getQueue()
            if (queue.isNotEmpty()) {
                queue.firstOrNull()?.let { first ->
                    queueRepo.markAsCurrent(first.id)
                    MusicPlayerManager.resolveUrl(first.play_url)?.run {
                        MusicPlayerManager.play(this, first.song_id)
                    }
                }
            }
        }
    }
}