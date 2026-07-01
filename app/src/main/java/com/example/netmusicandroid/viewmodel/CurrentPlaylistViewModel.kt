package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.netmusicandroid.data.model.SongItem
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import kotlinx.coroutines.flow.map

/**
 * 当前播放列表ViewModel
 * 负责管理播放队列数据，提供UI可观察的歌曲列表
 */
class CurrentPlaylistViewModel : ViewModel() {

    // 播放队列数据仓库，操作本地数据库播放队列
    private val repo = PlayQueueRepository.getInstance()

    /**
     * 播放队列歌曲列表LiveData
     * 1. 从仓库获取数据库实体Flow数据流
     * 2. 将数据库实体PlayQueueEntity转为前端展示模型SongItem
     * 3. Flow转LiveData供页面观察刷新UI
     */
    val songs: LiveData<List<SongItem>> = repo.observeQueue()
        .map { list -> list.map { it.toSongItem() } }
        .asLiveData()

    /**
     * 数据库播放队列实体 转 UI展示用SongItem实体扩展函数
     * @return 页面展示歌曲数据模型
     */
    private fun com.example.netmusicandroid.data.db.PlayQueueEntity.toSongItem() = SongItem(
        song_id = song_id,                // 歌曲id
        song_name = song_name,            // 歌曲名
        singer_id = 0,                    // 歌手id（暂无赋值，默认0）
        singer_name = singer_name,         // 歌手名称
        cover_url = cover_url,            // 封面图片地址
        play_url = play_url ?: "",        // 播放链接，空则赋空字符串
        duration = duration ?: 0,         // 歌曲时长，空则赋0
        added_at = ""                     // 添加时间，暂不赋值
    )
}