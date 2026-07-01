package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.UserPlaylist
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager

class HomePlaylistAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<HomePlaylistAdapter.VH>() {

    private var data: List<UserPlaylist> = emptyList()

    fun submitList(list: List<UserPlaylist>) {
        data = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.tvName.text = item.playlist_name
        holder.tvCount.text = "${item.song_count}首"
        ImageLoadUtil.loadImage(holder.ivCover, MusicPlayerManager.resolveUrl(item.cover_url))
        holder.itemView.setOnClickListener { onItemClick(item.playlist_id) }
    }

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivPlaylistCover)
        val tvName: TextView = view.findViewById(R.id.tvPlaylistName)
        val tvCount: TextView = view.findViewById(R.id.tvSongCount)
    }
}