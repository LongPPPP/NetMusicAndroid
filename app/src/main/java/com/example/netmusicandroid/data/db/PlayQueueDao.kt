package com.example.netmusicandroid.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlayQueueDao {

    // ── 增 ──────────────────────────────────────

    /** 追加单首歌曲到队尾，返回新行 id */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun appendSong(song: PlayQueueEntity): Long

    /** 批量追加（导入歌单到队列） */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun appendSongs(songs: List<PlayQueueEntity>)

    // ── 删 ──────────────────────────────────────

    /** 按主键删除单首 */
    @Delete
    abstract suspend fun deleteSong(song: PlayQueueEntity)

    /** 按主键删除单首（无需先查询实体） */
    @Query("DELETE FROM play_queue WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    /** 按歌曲 ID 删除（用于外部知道 song_id 的场景） */
    @Query("DELETE FROM play_queue WHERE song_id = :songId")
    abstract suspend fun deleteBySongId(songId: Int)

    /** 清空整个播放队列 */
    @Query("DELETE FROM play_queue")
    abstract suspend fun clearQueue()

    // ── 改 ──────────────────────────────────────

    /**
     * 将指定 id 的行标记为当前播放（原子操作：先清除所有标记，再设置目标行）
     */
    @Transaction
    open suspend fun markAsCurrent(id: Long) {
        clearCurrentFlag()
        setCurrentById(id)
    }

    /** 清除所有歌曲的 is_current 标记 */
    @Query("UPDATE play_queue SET is_current = 0")
    abstract suspend fun clearCurrentFlag()

    /** 将指定 id 的行标记为当前播放 */
    @Query("UPDATE play_queue SET is_current = 1 WHERE id = :id")
    abstract suspend fun setCurrentById(id: Long)

    /** 更新歌曲的排序位置 */
    @Query("UPDATE play_queue SET sort_order = :newOrder WHERE id = :id")
    abstract suspend fun updateSortOrder(id: Long, newOrder: Int)

    // ── 查 ──────────────────────────────────────

    /** 当前队列中最大的 sort_order，没有歌曲时返回 null */
    @Query("SELECT MAX(sort_order) FROM play_queue")
    abstract suspend fun getMaxSortOrder(): Int?

    /** Flow 监听整个队列（按播放顺序升序），UI 自动刷新 */
    @Query("SELECT * FROM play_queue ORDER BY sort_order ASC")
    abstract fun observeQueue(): Flow<List<PlayQueueEntity>>

    /** 一次性获取整个队列 */
    @Query("SELECT * FROM play_queue ORDER BY sort_order ASC")
    abstract suspend fun getQueue(): List<PlayQueueEntity>

    /** 获取当前播放歌曲 */
    @Query("SELECT * FROM play_queue WHERE is_current = 1 LIMIT 1")
    abstract suspend fun getCurrentSong(): PlayQueueEntity?

    /** Flow 监听当前播放歌曲 */
    @Query("SELECT * FROM play_queue WHERE is_current = 1 LIMIT 1")
    abstract fun observeCurrentSong(): Flow<PlayQueueEntity?>

    /** 下一首（当前歌曲后第一首） */
    @Query(
        """
        SELECT * FROM play_queue
        WHERE sort_order > (SELECT sort_order FROM play_queue WHERE is_current = 1 LIMIT 1)
        ORDER BY sort_order ASC LIMIT 1
        """
    )
    abstract suspend fun getNextSong(): PlayQueueEntity?

    /** 上一首（当前歌曲前第一首） */
    @Query(
        """
        SELECT * FROM play_queue
        WHERE sort_order < (SELECT sort_order FROM play_queue WHERE is_current = 1 LIMIT 1)
        ORDER BY sort_order DESC LIMIT 1
        """
    )
    abstract suspend fun getPrevSong(): PlayQueueEntity?
}
