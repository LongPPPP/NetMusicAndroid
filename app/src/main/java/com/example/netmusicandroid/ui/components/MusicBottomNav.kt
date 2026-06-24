package com.example.netmusicandroid.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.netmusicandroid.ui.icons.VinylDiscIcon // 导入自定义图标

/**
 * 全局底部导航栏组件
 * @param currentTab 当前选中Tab
 * @param onTabClick Tab切换点击回调
 */
@Composable
fun BottomNavBar(
    currentTab: MusicTab,
    onTabClick: (MusicTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val tabList = MusicTab.getAllTabs()
        tabList.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabClick(tab) },
                icon = {
                    // 判断：自定义图标/系统Material图标
                    if (tab.useCustomIcon) {
                        VinylDiscIcon(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = tab.icon!!, // 非自定义tab一定有系统图标，非空
                            contentDescription = tab.tabName,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text(text = tab.tabName, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}