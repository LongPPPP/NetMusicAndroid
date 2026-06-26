package com.example.netmusicandroid.api

import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.sp.SpManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit全局单例工具类
 * 统一BaseUrl、请求头自动携带Token、日志打印
 */
object ApiClient {
    // 单例Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConst.BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // OkHttp客户端，添加请求头拦截器+日志拦截器
    private fun getOkHttpClient(): OkHttpClient {
        // 日志拦截器
        val logInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // Token请求头拦截器
        val tokenInterceptor = Interceptor { chain ->
            // 本地模拟固定Token，调试专用
            val testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgyNDcxMDQ3LCJleHAiOjE3ODI0NzE5NDd9.MMnlPop2PSu9O8kNV7PDn_67U1_A61MawCJrKRm4B20"
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $testToken")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(logInterceptor)
            .build()
    }

    // 获取接口实例
    val musicApi: MusicApi by lazy {
        retrofit.create(MusicApi::class.java)
    }
}