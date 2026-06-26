package com.example.netmusicandroid.api

/**
 * 后端全局统一返回JSON外层封装
 * @param code 状态码 200=成功，其余为错误码
 * @param message 提示文案
 * @param data 业务数据体，可为null
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T
)