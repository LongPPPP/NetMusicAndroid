package com.example.netmusicandroid.sp

import android.content.Context
import android.content.SharedPreferences
import com.example.netmusicandroid.MinMusicApp
import com.example.netmusicandroid.constant.SpConst

/**
 * SharedPreferences轻量持久化工具
 * 存储：当前登录邮箱、用户ID、登录状态标记等简单键值数据
 * Token(accessToken/refreshToken)统一存入Room UserEntity本地用户表，不再使用SP存储
 */
object SpManager {
    // 全局唯一SharedPreferences实例，使用Application全局上下文避免内存泄漏
    private val sp: SharedPreferences by lazy {
        MinMusicApp.globalContext.getSharedPreferences(
            SpConst.SP_FILE_NAME,
            Context.MODE_PRIVATE
        )
    }

    /**
     * 异步编辑SP，适用于大部分普通存储场景，不会阻塞主线程
     */
    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        val editor = sp.edit()
        editor.block()
        editor.apply()
    }

    /**
     * 同步编辑SP，立即写入磁盘
     * 登出、Token失效清空等需要即时生效的场景使用
     * @return 是否写入成功
     */
    private fun editSync(block: SharedPreferences.Editor.() -> Unit): Boolean {
        val editor = sp.edit()
        editor.block()
        return editor.commit()
    }

    // ===================== 当前登录邮箱 =====================
    fun setCurrentLoginEmail(email: String?) = edit {
        putString(SpConst.KEY_CURRENT_EMAIL, email)
    }

    fun getCurrentLoginEmail(): String? {
        return sp.getString(SpConst.KEY_CURRENT_EMAIL, null)
    }

    fun clearCurrentLoginEmail() = edit {
        remove(SpConst.KEY_CURRENT_EMAIL)
    }

    /**
     * 判断本地是否存在登录邮箱记录
     */
    fun hasLoginEmail(): Boolean = !getCurrentLoginEmail().isNullOrBlank()

    // ===================== 当前登录用户ID =====================
    fun setUserId(userId: Long) = edit {
        putLong(SpConst.KEY_USER_ID, userId)
    }

    fun getUserId(): Long {
        return sp.getLong(SpConst.KEY_USER_ID, 0L)
    }

    // ===================== 全局登录状态标记 =====================
    fun setLoginStatus(isLogin: Boolean) = edit {
        putBoolean(SpConst.KEY_IS_LOGIN, isLogin)
    }

    fun getLoginStatus(): Boolean {
        return sp.getBoolean(SpConst.KEY_IS_LOGIN, false)
    }

    // ===================== 批量清空SP登录数据 =====================
    /**
     * 异步清空全部SP缓存，普通页面登出使用
     */
    fun clearAll() = edit {
        clear()
    }

    /**
     * 同步清空全部SP缓存，Token刷新失败、强制登出场景使用
     * @return 清空操作是否执行成功
     */
    fun clearAllSync(): Boolean = editSync {
        clear()
    }
}