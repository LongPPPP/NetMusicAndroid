package com.example.netmusicandroid.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MusicTab(
    val tabName: String,
    // 系统图标，自定义图标时传null
    val icon: ImageVector? = null,
    // 标记是否使用自定义图标
    val useCustomIcon: Boolean = false
) {
    object Home : MusicTab(
        tabName = "主页",
        icon = Icons.Outlined.Home
    )

    object Recommend : MusicTab(
        tabName = "猜你听",
        useCustomIcon = true
    )

    object Mine : MusicTab(
        tabName = "我的",
        icon = Icons.Outlined.Person
    )

    companion object {
        fun getAllTabs(): List<MusicTab> = listOf(Home, Recommend, Mine)
    }
}