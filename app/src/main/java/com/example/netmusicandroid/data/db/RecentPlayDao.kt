package com.example.netmusicandroid.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {

    // ── 增 / 改 ────────────────────────────────

    /**
     * 记录播放历史：song_id 已存在则更新 played_at（覆盖），否则新增。
     * 返回新生成或被更新的行 id。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPlay(entity: RecentPlayEntity): Long

    // ── 删 ──────────────────────────────────────

    /** 按实体删除 */
    @Delete
    suspend fun delete(entity: RecentPlayEntity)

    /** 按主键删除 */
    @Query("DELETE FROM recent_plays WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 按歌曲 ID 删除 */
    @Query("DELETE FROM recent_plays WHERE song_id = :songId")
    suspend fun deleteBySongId(songId: Int)

    /** 清空全部播放历史 */
    @Query("DELETE FROM recent_plays")
    suspend fun clearAll()

    // ── 查 ──────────────────────────────────────

    /** Flow 监听全部播放历史（最近播放在前） */
    @Query("SELECT * FROM recent_plays ORDER BY played_at DESC")
    fun observeAll(): Flow<List<RecentPlayEntity>>

    /** 分页查询：limit=每页条数, offset=偏移量 */
    @Query("SELECT * FROM recent_plays ORDER BY played_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<RecentPlayEntity>

    /** 总记录数 */
    @Query("SELECT COUNT(*) FROM recent_plays")
    suspend fun getCount(): Int

    /** 查询超出上限的最旧记录的 id 列表（用于自动裁剪） */
    @Query(
        """
        SELECT id FROM recent_plays
        ORDER BY played_at DESC
        LIMIT -1 OFFSET :maxCount
        """
    )
    suspend fun getOverflowIds(maxCount: Int): List<Long>

    /** 按 id 列表批量删除 */
    @Query("DELETE FROM recent_plays WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
