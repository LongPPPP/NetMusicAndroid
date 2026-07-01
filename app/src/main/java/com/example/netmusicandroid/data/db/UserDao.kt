package com.example.netmusicandroid.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // 增/改：如果账号已存在，则替换它（达到更新最后登录时间的目的）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    // 查：获取所有历史登录账号，按时间倒序排（最近登录的在前）
    @Query("SELECT * FROM local_users ORDER BY lastLoginTime DESC")
    suspend fun getAllHistoryUsers(): List<UserEntity>

    // 删：删除某个特定的账号记忆
    @Delete
    suspend fun deleteUser(user: UserEntity)

    // 查：根据邮箱查特定用户（用于校验等）
    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserEntity?

    // 新增：流式监听指定邮箱用户的数据变化（UI自动刷新核心）
    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    fun observeUserByEmail(email: String): Flow<UserEntity?>
}