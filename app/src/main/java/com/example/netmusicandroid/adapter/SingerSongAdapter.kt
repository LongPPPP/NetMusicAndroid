package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.HotSong

class SingerSongAdapter(
    private var songList: List<HotSong>,
    private val onItemClick: (HotSong) -> Unit
) : RecyclerView.Adapter<SingerSongAdapter.SongViewHolder>() {

    fun updateData(newList: List<HotSong>) {
        songList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_singer_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        holder.tvIndex.text = (position + 1).toString()
        holder.tvSongName.text = song.song_name
        
        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    override fun getItemCount(): Int = songList.size

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIndex: TextView = view.findViewById(R.id.tvIndex)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
    }
}
