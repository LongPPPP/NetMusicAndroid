package com.example.netmusicandroid.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 单例。
 *
 * 切换模拟器/真机时只需改 BASE_URL。
 */
object ApiClient {

    /**
     * 后端基础地址。
     *
     * - 模拟器 → 10.0.2.2
     * - 真机   → 电脑局域网 IP（如 192.168.1.5）
     */
    private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

    // ============ 通用客户端（不需要 token 的接口用这个）============
    val client: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // ============ 带认证的客户端（需要 token 的接口用这个）============
    // 见第 11 节

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /** 日志拦截器：开发时把请求/响应打印到 Logcat（tag: OkHttp） */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 快捷方法：创建 API 接口实例
    inline fun <reified T> createService(): T = client.create(T::class.java)
}