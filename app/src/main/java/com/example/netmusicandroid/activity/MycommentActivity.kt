package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.MyCommentItem
import com.example.netmusicandroid.viewmodel.MyCommentViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MyCommentActivity : AppCompatActivity() {

    private lateinit var viewModel: MyCommentViewModel
    private lateinit var adapter: MyCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mycomment)

        viewModel = ViewModelProvider(this)[MyCommentViewModel::class.java]

        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvComments)
        adapter = MyCommentAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.loadNextPage()
                }
            }
        })

        viewModel.comments.observe(this) { list ->
            adapter.submitList(list)
            findViewById<View>(R.id.tvEmpty).visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
        viewModel.isLoading.observe(this) { loading ->
            findViewById<View>(R.id.progressBar).visibility =
                if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadFirstPage()
    }

    private class MyCommentAdapter : RecyclerView.Adapter<MyCommentAdapter.VH>() {

        private var data: List<MyCommentItem> = emptyList()
        private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun submitList(list: List<MyCommentItem>) {
            data = list
            notifyDataSetChanged()
        }

        override fun getItemCount() = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_comment, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.tvSong.text = item.song?.song_name ?: "未知歌曲"
            holder.tvContent.text = item.content
            holder.tvTime.text = formatTime(item.created_at)
        }

        private fun formatTime(iso: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                fmt.format(parser.parse(iso) ?: return iso)
            } catch (_: Exception) {
                iso
            }
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvSong: TextView = view.findViewById(R.id.tvSongName)
            val tvContent: TextView = view.findViewById(R.id.tvContent)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
        }
    }
}
