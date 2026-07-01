package com.example.netmusicandroid.data.api

import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.AvatarUploadData
import com.example.netmusicandroid.data.model.LoginData
import com.example.netmusicandroid.data.model.RefreshTokenData
import com.example.netmusicandroid.data.model.SingerIdData
import com.example.netmusicandroid.data.model.UserInfo
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

//定义接口 登录和注册里各请求体的具体内容
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val confirmPassword: String,
    val email: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class UpdateUserRequest(
    val field: String,
    val value: String
)

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<Unit>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginData>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshRequest): ApiResponse<RefreshTokenData>

    /** 修改用户信息 PATCH /users/me — field ∈ {signature, username, email} */
    @PATCH("users/me")
    suspend fun updateUser(@Body body: UpdateUserRequest): ApiResponse<UserInfo>

    /** 上传头像 PUT /users/me/avatar，服务端返回 {url: "/static/avatars/xxx.jpg"} */
    @Multipart
    @PUT("users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiResponse<AvatarUploadData>

    /** 查询当前用户绑定的歌手ID GET /users/me/singer */
    @GET("users/me/singer")
    suspend fun getMySinger(): ApiResponse<SingerIdData>
}