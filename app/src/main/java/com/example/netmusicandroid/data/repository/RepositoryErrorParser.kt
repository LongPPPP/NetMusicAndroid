package com.example.netmusicandroid.data.repository

import org.json.JSONObject
import retrofit2.HttpException

object RepositoryErrorParser {
    fun parse(e: Throwable, defaultMessage: String = "网络异常"): String {
        return if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                JSONObject(errorBody ?: "").optString("message").ifBlank {
                    e.message() ?: "请求错误"
                }
            } catch (ex: Exception) {
                e.message() ?: "请求错误"
            }
        } else {
            e.message ?: defaultMessage
        }
    }
}
