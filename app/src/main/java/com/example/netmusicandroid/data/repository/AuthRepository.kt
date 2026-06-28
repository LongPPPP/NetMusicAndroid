package com.example.netmusicandroid.data.repository

import com.example.netmusicandroid.data.api.ApiClient
import com.example.netmusicandroid.data.api.AuthApiService
import com.example.netmusicandroid.data.api.LoginRequest
import com.example.netmusicandroid.data.api.RegisterRequest

class AuthRepository {

    private val api =
        ApiClient.createService<AuthApiService>()

    //主线程不能直接联网,所以要加suspend修饰符,让这个函数可以挂起,可以执行耗时操作,比如等待服务器
    suspend fun login(
        email: String,
        password: String
    ) = api.login(
        LoginRequest(
            email,
            password
        )
    )

    suspend fun register(
        username: String,
        password: String,
        confirmPassword: String,
        email: String
    ) = api.register(
        RegisterRequest(
            username,
            password,
            confirmPassword,
            email
        )
    )
}