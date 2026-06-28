package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.SongDetail

class HomeSongAdapter(
    private var songList: List<SongDetail>,
    private val onItemClick: (SongDetail) -> Unit
) : RecyclerView.Adapter<HomeSongAdapter.SongViewHolder>() {

    fun updateData(newList: List<SongDetail>) {
        songList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        android.util.Log.d("CoverDebug", "正在绑定第 $position 首歌: ${song.song_name}")
        android.util.Log.d("CoverDebug", "原始 cover_url: ${song.cover_url}")

        holder.tvSongName.text = song.song_name
        holder.tvSingerName.text = song.singer_name
        
        // 处理图片 URL (严格处理 null 和 空格转义)
        val rawCoverUrl = song.cover_url ?: ""
        val coverUrl = when {
            rawCoverUrl.isEmpty() -> null
            rawCoverUrl.startsWith("http") -> rawCoverUrl.replace(" ", "%20")
            else -> "http://10.240.200.130:3000$rawCoverUrl".replace(" ", "%20")
        }
        
        android.util.Log.d("CoverDebug", "请求的封面地址: $coverUrl")

        Glide.with(holder.itemView.context)
            .load(coverUrl)
            .placeholder(R.drawable.disk)//如果正在加载，显示 disk
            .error(R.drawable.disk)//如果地址是 null 或者加载失败，显示 disk
            .into(holder.ivCover)

        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    override fun getItemCount(): Int = songList.size

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivSongCover)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
        val tvSingerName: TextView = view.findViewById(R.id.tvSingerName)
    }
}
