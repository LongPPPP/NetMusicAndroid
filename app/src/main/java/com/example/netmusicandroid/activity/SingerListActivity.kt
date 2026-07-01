package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.model.SingerItem
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.SingerListViewModel

class SingerListActivity : AppCompatActivity() {

    private lateinit var viewModel: SingerListViewModel
    private lateinit var adapter: SingerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signerlist)

        viewModel = ViewModelProvider(this)[SingerListViewModel::class.java]

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        val lvSinger = findViewById<ListView>(R.id.lv_singer)
        adapter = SingerAdapter { singer ->
            startActivity(Intent(this, SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            })
        }
        lvSinger.adapter = adapter

        // 搜索框 → 跳转 SearchActivity
        findViewById<View>(R.id.rl_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // 观察数据
        viewModel.singers.observe(this) { adapter.updateData(it) }
        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }

        viewModel.loadSingers()
    }

    // ── 简易 ListView 适配器 ────────────────────

    private class SingerAdapter(
        private val onClick: (SingerItem) -> Unit
    ) : BaseAdapter() {

        private var data: List<SingerItem> = emptyList()

        fun updateData(list: List<SingerItem>) {
            data = list
            notifyDataSetChanged()
        }

        override fun getCount() = data.size
        override fun getItem(pos: Int) = data[pos]
        override fun getItemId(pos: Int) = pos.toLong()

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: VH
            if (convertView == null) {
                view = LayoutInflater.from(parent!!.context)
                    .inflate(R.layout.item_signer_list, parent, false)
                holder = VH(
                    view.findViewById(R.id.iv_avatar),
                    view.findViewById(R.id.tv_name)
                )
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as VH
            }
            val item = getItem(pos)
            holder.tvName.text = item.singer_name
            ImageLoadUtil.loadImage(holder.ivAvatar, MusicPlayerManager.resolveUrl(item.avatar_url))
            view.setOnClickListener { onClick(item) }
            return view
        }

        private class VH(val ivAvatar: ImageView, val tvName: TextView)
    }
}
