package com.example.netmusicandroid.sp

import android.content.Context
import android.content.SharedPreferences
import com.example.netmusicandroid.MinMusicApp
import com.example.netmusicandroid.constant.SpConst

/**
 * SharedPreferences轻量持久化工具
 * 存储：登录Token、用户ID、基础登录状态等简单键值数据
 */
object SpManager {
    // 全局唯一SharedPreferences实例
    private val sp: SharedPreferences by lazy {
        MinMusicApp.globalContext.getSharedPreferences(
            SpConst.SP_FILE_NAME,
            Context.MODE_PRIVATE
        )
    }

    // ===================== Token 存取（接口鉴权Bearer Token核心） =====================
    fun setToken(token: String) {
        sp.edit().putString(SpConst.KEY_TOKEN, token).apply()
    }

    fun getToken(): String {
        return sp.getString(SpConst.KEY_TOKEN, "") ?: ""
    }

    // 清空Token（退出登录时调用）
    fun clearToken() {
        sp.edit().remove(SpConst.KEY_TOKEN).apply()
    }

    // ===================== 用户ID =====================
    fun setUserId(userId: Long) {
        sp.edit().putLong(SpConst.KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return sp.getLong(SpConst.KEY_USER_ID, 0L)
    }

    // ===================== 登录状态标记 =====================
    fun setLoginStatus(isLogin: Boolean) {
        sp.edit().putBoolean(SpConst.KEY_IS_LOGIN, isLogin).apply()
    }

    fun isUserLogin(): Boolean {
        return sp.getBoolean(SpConst.KEY_IS_LOGIN, false)
    }

    // ===================== 通用清空全部存储（退出登录一键清理） =====================
    fun clearAll() {
        sp.edit().clear().apply()
    }
}