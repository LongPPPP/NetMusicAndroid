package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.data.model.SongItem
import com.example.netmusicandroid.databinding.ItemItemSongBinding
import com.example.netmusicandroid.utils.ImageLoadUtil

class SongListAdapter(
    // 删除回调：传 song_id(Int)
    private val onSongDeleteClick: (songId: Int) -> Unit
) : ListAdapter<SongItem, SongListAdapter.SongVH>(SongDiffCallback()) {

    inner class SongVH(val binding: ItemItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongItem, position: Int) {
            // 1. 条目序号
            binding.tvIndex.text = (position + 1).toString()

            // 2. 文本赋值（匹配你现有实体字段）
            binding.tvSongName.text = item.song_name
            binding.tvArtist.text = item.singer_name
            // 将秒数duration转为 04:38 格式工具方法
            binding.tvDuration.text = formatDuration(item.duration)

            // 3. 加载歌曲封面
            ImageLoadUtil.loadImage(binding.ivSongCover, item.cover_url)

            // 4. 删除按钮点击回调，传递song_id
            binding.ivDelete.setOnClickListener {
                onSongDeleteClick.invoke(item.song_id)
            }
        }

        // 工具：秒数转 mm:ss
        private fun formatDuration(totalSecond: Int): String {
            val minute = totalSecond / 60
            val second = totalSecond % 60
            return String.format("%02d:%02d", minute, second)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val binding = ItemItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongVH(binding)
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        val song = getItem(position)
        holder.bind(song, position)
    }

    // DiffUtil 对比规则
    class SongDiffCallback : DiffUtil.ItemCallback<SongItem>() {
        override fun areItemsTheSame(oldItem: SongItem, newItem: SongItem): Boolean {
            return oldItem.song_id == newItem.song_id
        }

        override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem): Boolean {
            return oldItem == newItem
        }
    }
}