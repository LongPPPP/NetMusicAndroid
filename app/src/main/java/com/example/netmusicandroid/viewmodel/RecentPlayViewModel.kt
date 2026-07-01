package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.RecentPlayEntity
import com.example.netmusicandroid.data.model.SongItem
import com.example.netmusicandroid.data.repository.RecentPlayRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RecentPlayViewModel : ViewModel() {

    private val repo = RecentPlayRepository.getInstance()

    // ── 数据 ────────────────────────────────────

    /** Room Flow → 映射为 SongItem → LiveData，自动随数据库变化刷新 */
    val recentPlaySongs: LiveData<List<SongItem>> = repo.observeAll()
        .map { entities -> entities.map { it.toSongItem() } }
        .asLiveData()

    // ── Toast ───────────────────────────────────

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    fun clearToast() {
        _toastMsg.postValue("")
    }

    // ── 操作 ────────────────────────────────────

    /** 从播放历史中移除单首歌曲 */
    fun deleteBySongId(songId: Int) {
        viewModelScope.launch {
            repo.deleteBySongId(songId)
        }
    }

    /** 清空全部播放历史 */
    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
            _toastMsg.postValue("已清空播放历史")
        }
    }

    /** 播放全部（预留） */
    fun playAll() {
        _toastMsg.postValue("开始播放全部历史歌曲")
    }

    // ── 映射 ────────────────────────────────────

    /**
     * RecentPlayEntity → SongItem 字段映射。
     *
     * SongListAdapter 仅绑定 song_id / song_name / singer_name / duration / cover_url，
     * 其余字段（singer_id / play_url / added_at）填默认值不影响渲染。
     */
    private fun RecentPlayEntity.toSongItem() = SongItem(
        song_id = song_id,
        song_name = song_name,
        singer_id = 0,
        singer_name = singer_name,
        cover_url = cover_url,
        play_url = play_url ?: "",
        duration = duration ?: 0,
        added_at = ""
    )
}
