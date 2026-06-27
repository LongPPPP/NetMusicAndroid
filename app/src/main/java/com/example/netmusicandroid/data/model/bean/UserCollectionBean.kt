package com.example.netmusicandroid.data.model.bean
import com.google.gson.annotations.SerializedName

/**
 * 我的收藏歌单实体，接口 /api/v1/user/collections 返回
 */
data class UserCollectionBean(
    @SerializedName("playlist_id")
    val collectionId: Int,     // 歌单唯一ID，删除接口路径参数

    @SerializedName("playlist_name")
    val collectionName: String,  // 歌单名称

    @SerializedName("song_count")
    val songCount: Int,          // 歌曲数量

    @SerializedName("created_at")
    val createdAt: String,       // 创建时间

    // 当前接口返回JSON无封面、私密字段，给默认值防止解析报错
    val coverUrl: String = "",
    val isPrivate: Boolean = false
)