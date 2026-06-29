package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 全局互斥锁：同一时间仅能执行一次刷新Token，杜绝并发重复发起refresh请求
private val refreshMutex = Mutex()
// 内存缓存有效AccessToken，减少重复数据库查询
var cachedAccessToken: String? = null

object ApiClient {
    // 外部注入AuthRepository，解除硬耦合
    var authRepositoryProvider: (() -> AuthRepository)? = null

    val client: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConst.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val tokenInterceptor = Interceptor { chain ->
            val originRequest = chain.request()
            val path = originRequest.url.encodedPath
            // 过滤登录、刷新接口，不携带Token，防止401死循环
            val skipPaths = listOf("/auth/login", "/auth/refresh")
            if (skipPaths.any { path.contains(it) }) {
                return@Interceptor chain.proceed(originRequest)
            }

            val authRepo = authRepositoryProvider?.invoke()
                ?: throw IllegalStateException("请在Application初始化authRepositoryProvider")

            // 优先读取内存缓存，无缓存再查库
            fun getLocalToken(): String {
                cachedAccessToken?.takeIf { it.isNotEmpty() }?.let { return it }
                return runBlocking(Dispatchers.IO) {
                    authRepo.getValidAccessToken().getOrNull() ?: ""
                }.also { cachedAccessToken = it }
            }

            // 首次请求携带token
            val firstToken = getLocalToken()
            val firstReq = originRequest.newBuilder().apply {
                if (firstToken.isNotEmpty()) header("Authorization", "Bearer $firstToken")
            }.build()
            var response = chain.proceed(firstReq)

            // 401鉴权失败处理
            if (response.code == 401) {
                response.close()
                val newToken = runBlocking(Dispatchers.IO) {
                    refreshMutex.withLock {
                        // 锁内二次校验缓存，避免重复刷新
                        val cacheToken = cachedAccessToken
                        if (!cacheToken.isNullOrEmpty()) return@withLock cacheToken

                        val refreshRes = authRepo.refreshAccessToken()
                        val token = refreshRes.getOrNull()
                        cachedAccessToken = token
                        token
                    }
                }

                newToken?.let { validToken ->
                    // 新token重试请求
                    val retryReq = originRequest.newBuilder()
                        .header("Authorization", "Bearer $validToken")
                        .build()
                    response = chain.proceed(retryReq)
                    // 重试后仍401，凭证彻底失效
                    if (response.code == 401) {
                        handleTokenInvalid(authRepo)
                    }
                } ?: run {
                    // 刷新token失败
                    handleTokenInvalid(authRepo)
                }
            }
            return@Interceptor response
        }

        // 日志拦截器放最上层，完整打印请求响应body
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .build()
    }

    /** Token失效统一处理逻辑 */
    private fun handleTokenInvalid(authRepo: AuthRepository) {
        cachedAccessToken = null
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                authRepo.logout()
            }
            // 仅前台时跳转登录页，避免后台弹窗崩溃
            if (BaseActivity.isAppForeground()) {
                BaseActivity.globalGoLogin()
            }
        }
    }

    // 泛型快速创建Api服务实例
    inline fun <reified T> createService(): T = client.create(T::class.java)
}