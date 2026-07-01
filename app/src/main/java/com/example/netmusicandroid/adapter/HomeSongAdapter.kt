package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.SongDetail

class HomeSongAdapter(
    private val onItemClick: (SongDetail) -> Unit
) : ListAdapter<SongDetail, HomeSongAdapter.SongViewHolder>(SongDiffCallback()) {

    fun submitSongs(newList: List<SongDetail>) {
        submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        val holder = SongViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(getItem(position))
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.tvSongName.text = song.song_name
        holder.tvSingerName.text = song.singer_name

        val rawCoverUrl = song.cover_url ?: ""
        val baseHost = ApiConst.BASE_URL.replace("/api/v1/", "")
        val coverUrl = when {
            rawCoverUrl.isEmpty() -> null
            rawCoverUrl.startsWith("http") -> rawCoverUrl.replace(" ", "%20")
            else -> {
                val path = if (rawCoverUrl.startsWith("/")) rawCoverUrl else "/$rawCoverUrl"
                "$baseHost$path".replace(" ", "%20")
            }
        }

        Glide.with(holder.itemView.context)
            .load(coverUrl)
            .placeholder(R.drawable.disk)
            .error(R.drawable.disk)
            .into(holder.ivCover)
    }

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivSongCover)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
        val tvSingerName: TextView = view.findViewById(R.id.tvSingerName)
    }

    class SongDiffCallback : DiffUtil.ItemCallback<SongDetail>() {
        override fun areItemsTheSame(oldItem: SongDetail, newItem: SongDetail): Boolean {
            return oldItem.song_id == newItem.song_id
        }

        override fun areContentsTheSame(oldItem: SongDetail, newItem: SongDetail): Boolean {
            return oldItem == newItem
        }
    }
}
