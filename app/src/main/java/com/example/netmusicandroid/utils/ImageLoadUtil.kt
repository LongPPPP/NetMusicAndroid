package com.example.netmusicandroid.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R

object ImageLoadUtil {
    /**
     * 加载网络图片到 ImageView。
     * @param url 为 null 或空时加载默认占位图。
     */
    fun loadImage(iv: ImageView, url: String?) {
        Glide.with(iv.context)
            .load(url)
            .placeholder(R.drawable.ic_default_cover)
            .error(R.drawable.ic_default_cover)
            .centerCrop()
            .into(iv)
    }
}