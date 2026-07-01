package com.example.netmusicandroid.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.netmusicandroid.data.model.UserInfo
import com.google.gson.Gson

@Deprecated(
    message = "Token 和用户状态已迁移到 Room UserEntity + SpManager。新代码不要使用 SpUtil。",
    replaceWith = ReplaceWith("SpManager", "com.example.netmusicandroid.sp.SpManager")
)
object SpUtil {
    private const val SP_NAME = "user_sp"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_INFO = "user_info_json"
    private val gson = Gson()

    private fun getSp(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    // 保存token
    fun saveToken(context: Context, token: String) {
        getSp(context).edit().putString(KEY_TOKEN, token).apply()
    }

    // 获取本地token
    fun getToken(context: Context): String? {
        return getSp(context).getString(KEY_TOKEN, null)
    }

    // 判断是否已登录（token存在即视为登录）
    fun isLogin(context: Context): Boolean {
        return !getToken(context).isNullOrEmpty()
    }

    // 保存完整用户信息
    fun saveUserInfo(context: Context, userInfo: UserInfo) {
        val json = gson.toJson(userInfo)
        getSp(context).edit().putString(KEY_USER_INFO, json).apply()
    }

    // 读取本地缓存用户信息
    fun getUserInfo(context: Context): UserInfo? {
        val jsonStr = getSp(context).getString(KEY_USER_INFO, null) ?: return null
        return try {
            gson.fromJson(jsonStr, UserInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // 清空所有登录缓存（退出登录）
    fun clearUser(context: Context) {
        getSp(context).edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_INFO)
            .apply()
    }
}