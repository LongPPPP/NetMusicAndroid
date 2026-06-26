package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.LoginData
import com.example.netmusicandroid.data.model.RefreshTokenData
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<Unit>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginData>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshRequest): ApiResponse<RefreshTokenData>
}