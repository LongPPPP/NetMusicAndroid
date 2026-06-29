package com.example.netmusicandroid.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 播放队列持久化实体
 *
 * sort_order 决定播放顺序（升序），当前播放歌曲由 is_current 标记。
 * APP 重启后通过查询 sort_order 升序即可恢复完整队列。
 */
@Entity(tableName = "play_queue")
data class PlayQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "song_id")
    val song_id: Int,

    @ColumnInfo(name = "song_name")
    val song_name: String,

    @ColumnInfo(name = "singer_name")
    val singer_name: String,

    @ColumnInfo(name = "play_url")
    val play_url: String?,

    @ColumnInfo(name = "cover_url")
    val cover_url: String?,

    @ColumnInfo(name = "duration")
    val duration: Int?,

    /** 播放顺序，升序排列。新歌追加时取 MAX(sort_order) + 1 */
    @ColumnInfo(name = "sort_order")
    val sort_order: Int,

    /** 是否为当前播放歌曲（同队列仅一条为 true） */
    @ColumnInfo(name = "is_current")
    val is_current: Boolean = false
)
