package com.example.netmusicandroid

import android.app.Application
import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.repository.AuthRepository

/**
 * APP全局应用入口，APP进程创建时最先执行
 * 后续统一在这里初始化SP、数据库、播放器等全局工具
 */
class MinMusicApp : Application() {
    // 全局静态上下文，供ToastUtil等工具类无参调用
    companion object {
        lateinit var globalContext: MinMusicApp
    }

    override fun onCreate() {
        super.onCreate()
        // 赋值全局上下文
        globalContext = this

        // 1. 初始化Room数据库
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        val playQueueDao = db.playQueueDao()
        val recentPlayDao = db.recentPlayDao()
        // 赋值全局Dao给全局单例调用
        AppDatabase.globalUserDao = userDao
        AppDatabase.globalPlayQueueDao = playQueueDao
        AppDatabase.globalRecentPlayDao = recentPlayDao

        // 2. 一次性初始化AuthRepository单例（使用新的initRepo方法）
        AuthRepository.initRepo(userDao)

        // 注入AuthRepository到ApiClient，解除硬耦合依赖
        ApiClient.authRepositoryProvider = { AuthRepository.getInstance() }

        // 3. 最后再触发Retrofit懒加载，避免拦截器提前执行造成递归崩溃
        ApiClient.client
    }
}