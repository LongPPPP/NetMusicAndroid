package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.model.SingerItem

class HomeSingerAdapter(
    private var singerList: List<SingerItem>,
    private val onItemClick: (SingerItem) -> Unit
) : RecyclerView.Adapter<HomeSingerAdapter.SingerViewHolder>() {

    fun updateData(newList: List<SingerItem>) {
        singerList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_singer, parent, false)
        return SingerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        val singer = singerList[position]
        holder.tvSingerName.text = singer.singer_name
        
        val baseHost = ApiConst.BASE_URL.replace("/api/v1/", "").trimEnd('/')
        val avatarUrl = if (singer.avatar_url?.startsWith("http") == true) singer.avatar_url 
                       else "$baseHost${singer.avatar_url}"

        Glide.with(holder.itemView.context)
            .load(avatarUrl)
            .placeholder(R.drawable.music)
            .circleCrop()
            .into(holder.ivAvatar)
        
        holder.itemView.setOnClickListener { onItemClick(singer) }
    }

    override fun getItemCount(): Int = singerList.size

    class SingerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivSingerAvatar)
        val tvSingerName: TextView = view.findViewById(R.id.tvSingerName)
    }
}
