package com.example.netmusicandroid.data.model

// 单条评论条目信息
data class CommentItem(
    val comment_id: Int,
    val user_id: Int,
    val username: String,
    val content: String
)

// 评论列表分页数据
data class CommentListData(
    val list: List<CommentItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)
