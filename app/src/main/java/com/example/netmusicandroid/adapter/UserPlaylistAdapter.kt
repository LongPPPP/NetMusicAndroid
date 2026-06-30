package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.netmusicandroid.R
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.UserPlaylist
import com.example.netmusicandroid.databinding.ItemUserCollectionBinding
import com.example.netmusicandroid.utils.MusicPlayerManager

/**
 * 用户歌单列表适配器
 * 使用ListAdapter+DiffUtil实现局部刷新，性能优于传统RecyclerView.Adapter
 * @param onItemClick 歌单项点击回调，传入歌单ID
 * @param onDeleteClick 删除按钮点击回调，传入歌单ID
 */
class UserPlaylistAdapter(
    private val onItemClick: (playlistId: Int) -> Unit,
    private val onDeleteClick: (playlistId: Int) -> Unit
) : ListAdapter<UserPlaylist, UserPlaylistAdapter.CollectionVH>(CollectionDiffCallback()) {

    /**
     * ViewHolder：持有Item布局binding，负责数据绑定渲染
     */
    inner class CollectionVH(val binding: ItemUserCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * 绑定单条歌单数据到布局控件
         * @param item 单条用户歌单实体
         */
        fun bind(item: UserPlaylist) {
            // 整行item点击事件
            itemView.setOnClickListener {
                onItemClick.invoke(item.playlist_id)
            }

            // 设置歌单名称
            binding.tvCollectionName.text = item.playlist_name
            // 设置歌曲数量文案
            binding.tvSongNum.text = "${item.song_count}首"

            // 处理封面图片完整访问地址
            val url = MusicPlayerManager.resolveUrl(item.cover_url)
            // Glide加载圆形封面图
            Glide.with(binding.ivCover.context)
                .load(url)
                .placeholder(R.drawable.ic_default_cover) // 加载中占位图
                .error(R.drawable.ic_default_cover)       // 加载失败兜底图
                .transform(CircleCrop())                 // 圆形裁剪转换
                .into(binding.ivCover)

            // 删除图标点击事件
            binding.ivDelete.setOnClickListener {
                onDeleteClick.invoke(item.playlist_id)
            }
        }
    }

    /**
     * 创建ViewHolder，inflate item布局
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionVH {
        val bind = ItemUserCollectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CollectionVH(bind)
    }

    /**
     * 给对应位置ViewHolder绑定数据
     */
    override fun onBindViewHolder(holder: CollectionVH, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil差异比较器：用于ListAdapter对比新旧列表，只刷新变更Item
     */
    class CollectionDiffCallback : DiffUtil.ItemCallback<UserPlaylist>() {
        /**
         * 判断两条数据是否为同一条数据（唯一标识对比）
         */
        override fun areItemsTheSame(oldItem: UserPlaylist, newItem: UserPlaylist): Boolean {
            return oldItem.playlist_id == newItem.playlist_id
        }

        /**
         * 同一ID下，判断内容是否完全一致，不一致则刷新UI
         * 依赖UserPlaylist数据类重写equals方法
         */
        override fun areContentsTheSame(oldItem: UserPlaylist, newItem: UserPlaylist): Boolean {
            return oldItem == newItem
        }
    }
}