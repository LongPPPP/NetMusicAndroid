package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.SingerItem
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager

/**
 * 搜索结果歌手列表适配器。
 * 简易布局：封面头像 + 歌手名，点击跳转 SingerActivity。
 */
class SearchSingerAdapter(
    private val onItemClick: (SingerItem) -> Unit
) : ListAdapter<SingerItem, SearchSingerAdapter.VH>(DiffCallback()) {

    /**
     * ViewHolder：itemView 必须是 inflate 返回的根 View（LinearLayout），
     * 不能传入子 View（ivAvatar），否则 RecyclerView attach 时触发
     * IllegalStateException: ViewHolder views must not be attached when created.
     */
    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_singer_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_singer_name)

        fun bind(item: SingerItem) {
            tvName.text = item.singer_name
            ImageLoadUtil.loadImage(ivAvatar, MusicPlayerManager.resolveUrl(item.avatar_url))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_singer, parent, false)
        val vh = VH(view)
        // 点击事件只在创建时绑定一次，避免 onBind 重复设置
        vh.itemView.setOnClickListener {
            val pos = vh.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick(getItem(pos))
            }
        }
        return vh
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SingerItem>() {
        override fun areItemsTheSame(old: SingerItem, new: SingerItem) =
            old.singer_id == new.singer_id
        override fun areContentsTheSame(old: SingerItem, new: SingerItem) =
            old == new
    }
}
