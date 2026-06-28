package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 全局协程域：处理登出、清理Token、页面跳转等异步操作
private val interceptorScope = CoroutineScope(Dispatchers.IO)
// 全局互斥锁：同一时间仅能执行一次刷新Token，杜绝并发重复发起refresh请求
private val refreshMutex = Mutex()

object ApiClient {
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
            val isLoginApi = path.contains("/auth/login")
            val isRefreshTokenApi = path.contains("/auth/refresh")
            if (isLoginApi || isRefreshTokenApi) {
                return@Interceptor chain.proceed(originRequest)
            }

            var isRetried = false
            val authRepo = AuthRepository.getInstance()

            // IO线程读取本地有效AccessToken
            suspend fun getValidToken(): String = withContext(Dispatchers.IO) {
                authRepo.getValidAccessToken().getOrNull() ?: ""
            }

            // 首次请求添加Token头
            val firstToken = runBlocking { getValidToken() }
            val firstRequestBuilder = originRequest.newBuilder()
            if (firstToken.isNotEmpty()) {
                firstRequestBuilder.header("Authorization", "Bearer $firstToken")
            }
            var response = chain.proceed(firstRequestBuilder.build())

            // 鉴权401且未重试，执行刷新逻辑
            if (response.code == 401 && !isRetried) {
                response.close()
                isRetried = true

                val newToken = runBlocking {
                    refreshMutex.withLock {
                        // 锁内二次校验：排队线程复用已刷新的Token，避免重复请求刷新接口
                        val latestLocalToken = getValidToken()
                        if (latestLocalToken.isNotEmpty()) {
                            return@withLock latestLocalToken
                        }
                        // 发起远程刷新Token
                        val refreshResult = authRepo.refreshAccessToken()
                        refreshResult.getOrNull()
                    }
                }

                newToken?.let { validToken ->
                    // 使用新Token重试原始业务请求
                    val retryRequest = originRequest.newBuilder()
                        .header("Authorization", "Bearer $validToken")
                        .build()
                    response = chain.proceed(retryRequest)
                } ?: run {
                    // 刷新失败：refreshToken失效，清空本地登录信息 + 全局跳转登录页
                    interceptorScope.launch {
                        authRepo.logout()
                        // 调用BaseActivity静态全局跳转方法，无上下文也能跳转登录
                        BaseActivity.globalGoLogin()
                    }
                }
            }

            return@Interceptor response
        }

        // 日志拦截器放最上层，完整打印请求响应body，可查看refresh接口返回数据
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

    // 泛型快速创建Api服务实例
    inline fun <reified T> createService(): T = client.create(T::class.java)
}