package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
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
    private val onSongClick: ((SongItem) -> Unit)? = null,
    private val showDeleteButton: Boolean = true
) : ListAdapter<SongItem, SongListAdapter.SongVH>(SongDiffCallback()) {

    companion object {
        fun firstChangedIndexForPositionLabels(
            oldList: List<SongItem>,
            newList: List<SongItem>
        ): Int {
            val samePrefixCount = minOf(oldList.size, newList.size)
            for (index in 0 until samePrefixCount) {
                if (oldList[index].song_id != newList[index].song_id) return index
            }
            return if (oldList.size != newList.size) samePrefixCount else -1
        }
    }

    inner class SongVH(val binding: ItemItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongItem, position: Int) {
            binding.tvIndex.text = (position + 1).toString()
            binding.tvSongName.text = item.song_name
            binding.tvArtist.text = item.singer_name
            binding.tvDuration.text = formatDuration(item.duration)
            binding.ivDelete.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
            binding.ivDelete.isEnabled = showDeleteButton
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

    override fun submitList(list: List<SongItem>?) {
        val oldList = currentList.toList()
        super.submitList(list) {
            refreshPositionLabels(oldList, currentList)
        }
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        val song = getItem(position)
        holder.bind(song, position)
    }

    private fun refreshPositionLabels(oldList: List<SongItem>, newList: List<SongItem>) {
        val firstChangedIndex = firstChangedIndexForPositionLabels(oldList, newList)
        if (firstChangedIndex == -1 || firstChangedIndex >= itemCount) return
        notifyItemRangeChanged(firstChangedIndex, itemCount - firstChangedIndex)
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