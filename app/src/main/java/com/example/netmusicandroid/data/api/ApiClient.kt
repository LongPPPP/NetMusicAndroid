package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.sp.SpManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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
            val request = chain.request()
            val url = request.url.toString()
            
            // 调试日志：打印请求地址（魅族手机也能看到）
            println("ApiClient: Sending request to -> $url")

            val newRequestBuilder = request.newBuilder()
            
            // 排除登录和注册接口，不带 Token
            if (!url.contains("auth/login") && !url.contains("auth/register")) {
                val token = SpManager.getToken()
                if (token.isNotEmpty()) {
                    newRequestBuilder.addHeader("Authorization", "Bearer $token")
                }
            }
            
            chain.proceed(newRequestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // 改用 println，确保魅族手机 Logcat 能刷出来
            println("OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    inline fun <reified T> createService(): T =
        client.create(T::class.java)
}
