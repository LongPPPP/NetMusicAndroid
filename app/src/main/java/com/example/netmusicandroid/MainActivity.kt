package com.example.netmusicandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.netmusicandroid.ui.components.BottomNavBar
import com.example.netmusicandroid.ui.components.MiniPlayerBar
import com.example.netmusicandroid.ui.components.MusicTab
import com.example.netmusicandroid.ui.theme.NetMusicAndroidTheme
import com.example.netmusicandroid.activity.UserInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetMusicAndroidTheme {
                MainTabPage()
            }
        }
    }
}

@Composable
fun MainTabPage() {
    // 当前选中底部Tab
    var selectedTab: MusicTab by remember { mutableStateOf(MusicTab.Mine) }
    // 临时播放状态，后续替换全局ViewModel
    var isPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = selectedTab,
                onTabClick = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        // 提取底部导航自动计算的安全边距，防止页面内容被导航遮挡
        val bottomPadding = innerPadding.calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                // 左右统一20dp边距，顶部留少量空白
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
                // 底部自动留出导航栏高度，彻底解决遮挡
                .padding(bottom = bottomPadding)
        ) {
            // 页面主体区域，弹性占满剩余高度
            Column(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    MusicTab.Home -> HomeContentPage()
                    MusicTab.Recommend -> RecommendContentPage()
                    MusicTab.Mine -> MineContentPage()
                }
            }

            // 迷你播放栏，全局固定在导航栏上方
            MiniPlayerBar(
                coverRes = android.R.drawable.ic_menu_gallery, // 系统占位封面图
                songName = "Lover",
                artist = "Taylor Swift",
                isPlaying = isPlaying,
                onPlayClick = { isPlaying = !isPlaying }, // 切换播放/暂停状态
                onPrev = { /* 上一曲逻辑，后续对接后端API */ },
                onNext = { /* 下一曲逻辑，后续对接后端API */ },
                modifier = Modifier.padding(bottom = 8.dp) // 和底部导航留出间隙
            )
        }
    }
}

// 首页占位页面
@Composable
fun HomeContentPage() {
    Text("首页页面内容")
}

// 猜你听占位页面
@Composable
fun RecommendContentPage() {
    Text("猜你听页面内容")
}

// 我的页面：这里放你截图里完整的个人信息页面UI
@Composable
fun MineContentPage() {
    Column {
        // 后续把你的 TopActionRow / UserInfoSection / FunctionMenuCard 全部粘贴在这里
        UserInfo()
    }
}