package com.example.netmusicandroid.data.model


/**
 * 我的评论列表项 — 对应 GET /users/me/comments 的 list 元素
 */
data class MyCommentItem(
    val comment_id: Int,
    val content: String,
    val created_at: String,
    val song: MyCommentSong?
)

data class MyCommentSong(
    val song_id: Int,
    val song_name: String
)

data class MyCommentListData(
    val list: List<MyCommentItem>,
    val total: Int,
    val page: Int,
    val page_size: Int
)