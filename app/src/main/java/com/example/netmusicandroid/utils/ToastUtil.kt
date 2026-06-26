package com.example.netmusicandroid.utils

import android.widget.Toast
import com.example.netmusicandroid.MinMusicApp

object ToastUtil {
    private var cacheToast: Toast? = null

    // 短吐司
    fun showShort(msg: String) {
        cacheToast?.cancel()
        cacheToast = Toast.makeText(MinMusicApp.globalContext, msg, Toast.LENGTH_SHORT)
        cacheToast?.show()
    }

    // 长吐司（备用）
    fun showLong(msg: String) {
        cacheToast?.cancel()
        cacheToast = Toast.makeText(MinMusicApp.globalContext, msg, Toast.LENGTH_LONG)
        cacheToast?.show()
    }
}