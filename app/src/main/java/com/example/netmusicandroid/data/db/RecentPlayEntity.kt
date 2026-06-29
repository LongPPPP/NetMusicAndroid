package com.example.netmusicandroid.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 最近播放历史缓存实体
 *
 * 歌曲播放完成后自动记录；同一首歌曲重复播放时更新 played_at 时间戳。
 * 通过 played_at 降序排列展示最近播放列表。
 */
@Entity(
    tableName = "recent_plays",
    indices = [Index(value = ["song_id"], unique = true)]
)
data class RecentPlayEntity(
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

    /** 最近一次播放时间戳（毫秒），重复播放时更新为此时间 */
    @ColumnInfo(name = "played_at")
    val played_at: Long
)
