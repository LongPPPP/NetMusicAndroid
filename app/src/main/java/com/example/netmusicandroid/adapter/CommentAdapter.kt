package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.CommentItem
import com.example.netmusicandroid.sp.SpManager

class CommentAdapter(
    private var commentList: List<CommentItem>,
    private val onDeleteClick: (CommentItem) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // 更新数据的方法
    fun updateData(newList: List<CommentItem>) {
        commentList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.tvUsername.text = comment.username
        holder.tvContent.text = comment.content

        // 获取当前登录的用户ID
        val currentUserId = SpManager.getUserId().toInt()

        // 如果评论是当前用户发的，显示删除按钮
        if (comment.user_id == currentUserId) {
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivDelete.setOnClickListener {
                onDeleteClick(comment)
            }
        } else {
            holder.ivDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = commentList.size

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }
}
