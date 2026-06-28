package com.example.netmusicandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.db.UserEntity

class HistoryAccountAdapter(
    private var accountList: List<UserEntity>,
    private val onItemClick: (UserEntity) -> Unit,
    private val onDeleteClick: (UserEntity) -> Unit
) : RecyclerView.Adapter<HistoryAccountAdapter.ViewHolder>() {

    fun updateData(newList: List<UserEntity>) {
        accountList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = accountList[position]
        holder.tvEmail.text = user.email
        
        holder.itemView.setOnClickListener { onItemClick(user) }
        holder.ivDelete.setOnClickListener { onDeleteClick(user) }
    }

    override fun getItemCount(): Int = accountList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }
}
