package com.example.netmusicandroid.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column // 关键缺失导入
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 全局复用迷你底部播放栏
 * @param coverRes 本地封面资源ID，后续替换网络url字段
 * @param songName 歌曲名
 * @param artist 歌手名
 * @param isPlaying 是否正在播放
 * @param onPlayClick 播放/暂停回调
 * @param onPrev 上一曲
 * @param onNext 下一曲
 * @param onCoverClick 点击封面跳转播放详情
 */
@Composable
fun MiniPlayerBar(
    coverRes: Int,
    songName: String,
    artist: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onCoverClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 封面 + 歌名歌手
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = "歌曲封面",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onCoverClick() },
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Column {
                        Text(
                            text = songName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = artist,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 播放控制按钮组
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = "上一曲",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onPrev() }
                )
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = "播放暂停",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onPlayClick() }
                )
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = "下一曲",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onNext() }
                )
            }
        }
    }
}