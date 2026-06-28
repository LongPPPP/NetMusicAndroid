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

    // 统一引用 ApiConst里的BASE_URL
    val client: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConst.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val tokenInterceptor = Interceptor { chain ->
            val token = SpManager.getToken()
            val request = chain.request().newBuilder()
            if (token.isNotEmpty()) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    inline fun <reified T> createService(): T =
        client.create(T::class.java)
}
