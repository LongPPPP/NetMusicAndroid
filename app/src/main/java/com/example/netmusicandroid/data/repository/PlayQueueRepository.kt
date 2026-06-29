package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.db.PlayQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * 播放队列仓库
 *
 * 封装 Room Dao，提供队列持久化的增删改查能力。
 * 队列通过 sort_order 升序维护播放顺序，is_current 标记当前歌曲。
 *
 * 使用方式：
 * - 通过 observeQueue() / observeCurrentSong() 监听队列变化（Flow）
 * - 通过 append / remove / move / clear 修改队列
 * - 通过切歌方法 next / prev / skipTo 切换当前歌曲
 */
class PlayQueueRepository(
    private val dao: com.example.netmusicandroid.data.db.PlayQueueDao = AppDatabase.globalPlayQueueDao
) {

    // ── 监听（Flow） ────────────────────────────

    /** Flow 监听整个队列变化 */
    fun observeQueue(): Flow<List<PlayQueueEntity>> = dao.observeQueue()

    /** Flow 监听当前播放歌曲 */
    fun observeCurrentSong(): Flow<PlayQueueEntity?> = dao.observeCurrentSong()

    // ── 查询 ────────────────────────────────────

    /** 一次性获取队列快照 */
    suspend fun getQueue(): List<PlayQueueEntity> = dao.getQueue()

    /** 获取当前播放歌曲 */
    suspend fun getCurrentSong(): PlayQueueEntity? = dao.getCurrentSong()

    /** 获取下一首 */
    suspend fun getNextSong(): PlayQueueEntity? = dao.getNextSong()

    /** 获取上一首 */
    suspend fun getPrevSong(): PlayQueueEntity? = dao.getPrevSong()

    // ── 增 ──────────────────────────────────────

    /**
     * 追加单首歌曲到队尾，返回新行的 sort_order
     */
    suspend fun append(songId: Int, songName: String, singerName: String,
                       playUrl: String?, coverUrl: String?, duration: Int?): Int {
        val nextOrder = (dao.getMaxSortOrder() ?: 0) + 1
        val entity = PlayQueueEntity(
            song_id = songId,
            song_name = songName,
            singer_name = singerName,
            play_url = playUrl,
            cover_url = coverUrl,
            duration = duration,
            sort_order = nextOrder,
            is_current = false
        )
        dao.appendSong(entity)
        return nextOrder
    }

    /**
     * 批量追加歌曲到队尾（歌单导入等场景）
     */
    suspend fun appendAll(songs: List<PlayQueueEntity>) {
        if (songs.isEmpty()) return
        val startOrder = (dao.getMaxSortOrder() ?: 0) + 1
        val entities = songs.mapIndexed { index, song ->
            song.copy(sort_order = startOrder + index, is_current = false)
        }
        dao.appendSongs(entities)
    }

    // ── 删 ──────────────────────────────────────

    /** 按主键 id 移除 */
    suspend fun removeById(id: Long) = dao.deleteById(id)

    /** 按歌曲 ID 移除 */
    suspend fun removeBySongId(songId: Int) = dao.deleteBySongId(songId)

    /** 清空整个队列 */
    suspend fun clear() = dao.clearQueue()

    // ── 改 ──────────────────────────────────────

    /** 标记当前播放歌曲（原子操作：事务中先清除旧标记再设置新标记） */
    suspend fun markAsCurrent(id: Long) = dao.markAsCurrent(id)

    /** 交换两首歌的 sort_order（拖拽排序） */
    suspend fun swapOrder(idA: Long, idB: Long) {
        val queue = dao.getQueue()
        val a = queue.find { it.id == idA } ?: return
        val b = queue.find { it.id == idB } ?: return
        dao.updateSortOrder(idA, b.sort_order)
        dao.updateSortOrder(idB, a.sort_order)
    }

    // ── 切歌操作 ────────────────────────────────

    /** 切到下一首并返回，无下一首返回 null */
    suspend fun next(): PlayQueueEntity? {
        val next = dao.getNextSong() ?: return null
        dao.markAsCurrent(next.id)
        return next
    }

    /** 切到上一首并返回，无上一首返回 null */
    suspend fun prev(): PlayQueueEntity? {
        val prev = dao.getPrevSong() ?: return null
        dao.markAsCurrent(prev.id)
        return prev
    }
}
