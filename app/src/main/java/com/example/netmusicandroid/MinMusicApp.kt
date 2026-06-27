package com.example.netmusicandroid

import android.app.Application
import com.example.netmusicandroid.data.api.ApiClient

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

        // 原有逻辑：提前触发Retrofit懒加载，提前初始化网络客户端
        ApiClient.client

        // 后续可在这里添加：Room数据库初始化、SP工具初始化、播放器单例初始化等
    }
}