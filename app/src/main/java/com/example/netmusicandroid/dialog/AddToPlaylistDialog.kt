package com.example.netmusicandroid.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.UserPlaylist
import com.example.netmusicandroid.databinding.DialogAddToPlaylistBinding
import com.example.netmusicandroid.databinding.ItemPlaylistSelectBinding

/**
 * 添加到歌单弹窗 — 展示用户歌单列表，单选目标歌单后回调
 *
 * @param playlists 用户已有歌单列表
 * @param onConfirm 选中歌单后的回调，返回被选中的歌单实体
 */
class AddToPlaylistDialog(
    private val context: Context,
    private val playlists: List<UserPlaylist>,
    private val onConfirm: (UserPlaylist) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogAddToPlaylistBinding.inflate(LayoutInflater.from(context))
    private var selectedPosition: Int = -1
    private val adapter = PlaylistAdapter()

    fun show() {
        // 初始化RecyclerView
        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)
        binding.rvPlaylists.adapter = adapter

        // 关闭/取消
        val dismissAction = { dismiss() }
        binding.ivClose.setOnClickListener { dismissAction() }
        binding.btnCancel.setOnClickListener { dismissAction() }

        // 确认：将选中歌单回调
        binding.btnConfirm.setOnClickListener {
            if (selectedPosition in playlists.indices) {
                onConfirm(playlists[selectedPosition])
                dismiss()
            }
        }

        dialog = AlertDialog.Builder(context, R.style.FullTransDialogStyle)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        dialog?.show()

        // 弹窗宽度 90%，高度自适应
        dialog?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    // ── 内部适配器 ──────────────────────────────

    private inner class PlaylistAdapter :
        RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemPlaylistSelectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlist = playlists[position]
            holder.bind(playlist, position == selectedPosition)
        }

        override fun getItemCount(): Int = playlists.size

        inner class ViewHolder(
            private val itemBinding: ItemPlaylistSelectBinding
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(playlist: UserPlaylist, isSelected: Boolean) {
                itemBinding.tvPlaylistName.text = playlist.playlist_name
                itemBinding.tvSongCount.text = "${playlist.song_count}首"
                itemBinding.vSelectedBar.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                itemBinding.root.setBackgroundResource(
                    if (isSelected) R.drawable.shape_playlist_item_selected else android.R.color.transparent
                )

                itemBinding.root.setOnClickListener {
                    val oldPos = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(oldPos)
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }
}
