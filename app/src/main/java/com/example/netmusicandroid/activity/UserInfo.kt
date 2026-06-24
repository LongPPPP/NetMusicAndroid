package com.example.netmusicandroid.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netmusicandroid.R

/**
 * 我的页面完整UI，匹配截图样式
 */
@Composable
fun UserInfo() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. 顶部 通知 + 设置图标行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "通知",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(24.dp))
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "设置",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 头像 + 昵称 + 简介 + 编辑按钮
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // 圆形头像
            Image(
                painter = painterResource(id = R.drawable.ic_avatar),
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Music_Lover",
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑资料",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "音乐是生活的一部分。",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Lv.3",
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. 收藏 / 我的评论 数据行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "56", fontSize = 22.sp)
                Text(text = "收藏", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // 分割竖线
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "23", fontSize = 22.sp)
                Text(text = "我的评论", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 4. 功能菜单卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                MenuItem(icon = Icons.Outlined.Schedule, title = "最近播放")
                MenuItem(icon = Icons.Outlined.PlaylistPlay, title = "我的歌单")
                MenuItem(icon = Icons.Outlined.Settings, title = "设置")
                MenuItem(icon = Icons.Outlined.HelpOutline, title = "帮助与反馈")
            }
        }
    }
}

/**
 * 菜单条目通用组件：图标 + 文字 + 右箭头
 */
@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(text = ">", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
    }
}