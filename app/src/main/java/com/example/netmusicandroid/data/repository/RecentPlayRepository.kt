package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.db.RecentPlayDao
import com.example.netmusicandroid.data.db.RecentPlayEntity
import com.example.netmusicandroid.data.repository.RecentPlayRepository.Companion.MAX_COUNT
import kotlinx.coroutines.flow.Flow

/**
 * 最近播放历史仓库
 *
 * - 歌曲播放完成后自动调用 [record] 写入记录
 * - 同一首歌重复播放时更新 played_at 时间戳（OnConflictStrategy.REPLACE）
 * - 超过 [MAX_COUNT] 条后自动裁剪最旧记录
 * - 支持分页查询、Flow 监听、单条删除、清空
 *
 * 改造说明：改为全局单例模式，全应用共用同一份最近播放数据
 * 所有页面/ViewModel 操作的都是同一份数据，记录、删除全局自动同步
 */
class RecentPlayRepository private constructor(
    private val dao: RecentPlayDao
) {
    companion object {
        /** 最大缓存数量 */
        const val MAX_COUNT = 100

        @Volatile
        private var INSTANCE: RecentPlayRepository? = null

        /**
         * 初始化全局单例（仅在 Application 启动时调用一次）
         */
        fun initInstance(dao: RecentPlayDao) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = RecentPlayRepository(dao)
                    }
                }
            }
        }

        /**
         * 获取全局唯一实例
         * 调用前必须先在 Application.onCreate 中执行 initInstance
         */
        fun getInstance(): RecentPlayRepository {
            return INSTANCE ?: throw IllegalStateException(
                "RecentPlayRepository 未初始化，请在 Application.onCreate 中调用 initInstance"
            )
        }
    }

    // ── 监听（Flow） ────────────────────────────

    /** Flow 监听全部播放历史 */
    fun observeAll(): Flow<List<RecentPlayEntity>> = dao.observeAll()

    // ── 增 / 改 ────────────────────────────────

    /**
     * 记录一首播放完成的歌曲。
     * - 已存在（同 song_id）：更新 played_at 时间戳
     * - 不存在：新增记录
     * 写入后自动裁剪超出 MAX_COUNT 的旧记录。
     */
    suspend fun record(
        songId: Int,
        songName: String,
        singerName: String,
        playUrl: String?,
        coverUrl: String?,
        duration: Int?
    ) {
        val entity = RecentPlayEntity(
            song_id = songId,
            song_name = songName,
            singer_name = singerName,
            play_url = playUrl,
            cover_url = coverUrl,
            duration = duration,
            played_at = System.currentTimeMillis()
        )
        dao.recordPlay(entity)
        trimIfNeeded()
    }

    // ── 查 ──────────────────────────────────────

    /** 分页查询，返回最近播放列表 */
    suspend fun getPaged(page: Int, pageSize: Int = 20): List<RecentPlayEntity> {
        val offset = (page - 1).coerceAtLeast(0) * pageSize
        return dao.getPaged(pageSize, offset)
    }

    /** 总记录数 */
    suspend fun getCount(): Int = dao.getCount()

    // ── 删 ──────────────────────────────────────

    /** 删除单条记录 */
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /** 按歌曲 ID 删除 */
    suspend fun deleteBySongId(songId: Int) = dao.deleteBySongId(songId)

    /** 清空全部播放历史 */
    suspend fun clearAll() = dao.clearAll()

    // ── 内部 ────────────────────────────────────

    /** 超出上限时裁剪最旧的记录 */
    private suspend fun trimIfNeeded() {
        val overflow = dao.getOverflowIds(MAX_COUNT)
        if (overflow.isNotEmpty()) {
            dao.deleteByIds(overflow)
        }
    }
}