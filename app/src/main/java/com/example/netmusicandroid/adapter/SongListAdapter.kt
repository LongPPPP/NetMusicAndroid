package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.data.model.SongItem
import com.example.netmusicandroid.databinding.ItemItemSongBinding
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager

class SongListAdapter(
    private val onSongDeleteClick: (songId: Int) -> Unit,
    private val onSongClick: ((SongItem) -> Unit)? = null
) : ListAdapter<SongItem, SongListAdapter.SongVH>(SongDiffCallback()) {

    inner class SongVH(val binding: ItemItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongItem, position: Int) {
            binding.tvIndex.text = (position + 1).toString()
            binding.tvSongName.text = item.song_name
            binding.tvArtist.text = item.singer_name
            binding.tvDuration.text = formatDuration(item.duration)
            ImageLoadUtil.loadImage(binding.ivSongCover, MusicPlayerManager.resolveUrl(item.cover_url))
            binding.ivDelete.setOnClickListener {
                onSongDeleteClick.invoke(item.song_id)
            }
        }

        private fun formatDuration(totalSecond: Int): String {
            val minute = totalSecond / 60
            val second = totalSecond % 60
            return String.format("%02d:%02d", minute, second)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val binding = ItemItemSongBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val vh = SongVH(binding)
        vh.binding.root.setOnClickListener {
            val pos = vh.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onSongClick?.invoke(getItem(pos))
            }
        }
        return vh
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