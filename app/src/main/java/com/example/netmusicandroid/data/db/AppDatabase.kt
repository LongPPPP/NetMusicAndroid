package com.example.netmusicandroid.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        PlayQueueEntity::class,
        RecentPlayEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun playQueueDao(): PlayQueueDao
    abstract fun recentPlayDao(): RecentPlayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        lateinit var globalUserDao: UserDao
        lateinit var globalPlayQueueDao: PlayQueueDao
        lateinit var globalRecentPlayDao: RecentPlayDao

        // 获取数据库单例（保证全局只有一个数据库连接）
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "net_music_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}