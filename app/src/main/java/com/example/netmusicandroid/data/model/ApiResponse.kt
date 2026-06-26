package com.example.netmusicandroid.data.model

data class ApiResponse<T>(
    val code: Int,
    val message: String?,
    val data: T?
)