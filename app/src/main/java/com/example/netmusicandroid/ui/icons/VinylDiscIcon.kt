package com.example.netmusicandroid.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.netmusicandroid.R

/**
 * 猜你听页面自定义双圆环黑胶唱片图标
 */
@Composable
fun VinylDiscIcon(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_vinyl_outline),
        contentDescription = "猜你听",
        modifier = modifier.size(24.dp) // 统一Tab图标标准尺寸
    )
}