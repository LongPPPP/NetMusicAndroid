package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.data.model.SearchSongItem
import com.example.netmusicandroid.databinding.ItemItemSongBinding
import com.example.netmusicandroid.utils.ImageLoadUtil

/**
 * 搜索结果歌曲列表适配器。
 * 复用 item_item_song 布局，无删除按钮（仅展示歌名+歌手+封面+索引）。
 */
class SearchSongAdapter(
    private val onItemClick: (SearchSongItem) -> Unit
) : ListAdapter<SearchSongItem, SearchSongAdapter.VH>(DiffCallback()) {

    inner class VH(val binding: ItemItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchSongItem, position: Int) {
            binding.tvIndex.text = (position + 1).toString()
            binding.tvSongName.text = item.song_name
            binding.tvArtist.text = item.singer_name
            binding.tvDuration.text = ""  // 搜索结果无时长
            ImageLoadUtil.loadImage(binding.ivSongCover, item.cover_url)
            // 隐藏删除按钮（搜索结果不可删除）
            // INVISIBLE 而非 GONE：iv_delete 是 tv_duration 的 layout_toStartOf 锚点，
            // GONE 会导致 RelativeLayout 丢弃该定位规则，连锁摧毁 ll_song_info 的宽度计算
            binding.ivDelete.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemItemSongBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val vh = VH(binding)
        // 点击事件只在创建时绑定一次，避免 onBind 每次都重新 setOnClickListener
        vh.binding.root.setOnClickListener {
            val pos = vh.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick(getItem(pos))
            }
        }
        return vh
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchSongItem>() {
        override fun areItemsTheSame(old: SearchSongItem, new: SearchSongItem) =
            old.song_id == new.song_id
        override fun areContentsTheSame(old: SearchSongItem, new: SearchSongItem) =
            old == new
    }
}
