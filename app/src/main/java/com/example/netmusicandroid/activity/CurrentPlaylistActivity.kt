package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SongListAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.CurrentPlaylistViewModel
import kotlinx.coroutines.launch

class CurrentPlaylistActivity : AppCompatActivity() {

    private lateinit var viewModel: CurrentPlaylistViewModel // 当前播放列表VM
    private lateinit var bottomVm: BottomPlayerViewModel // 底部播放器VM
    private lateinit var songAdapter: SongListAdapter // 歌曲列表适配器
    private val songRepo = SongRepository.getInstance() // 歌曲数据仓库

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_playlist) // 加载播放列表布局

        viewModel = ViewModelProvider(this)[CurrentPlaylistViewModel::class.java] // 初始化列表VM
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java] // 初始化播放器VM

        findViewById<View>(R.id.ivBack).setOnClickListener { finish() } // 返回按钮关闭页面

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCollectionList) // 歌曲列表RecyclerView
        songAdapter = SongListAdapter(
            onSongDeleteClick = { }, // 删除歌曲逻辑暂空
            onSongClick = { songItem -> // 点击歌曲播放
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id) // 请求歌曲完整信息
                    result.onSuccess { detail ->
                        bottomVm.playSong(detail) // 更新播放器数据并播放
                    }
                }
            }
        )
        rv.layoutManager = LinearLayoutManager(this) // 线性布局
        rv.adapter = songAdapter // 绑定适配器

        viewModel.songs.observe(this) { list -> // 监听播放队列数据更新列表
            songAdapter.submitList(list)
            findViewById<android.widget.TextView>(R.id.tvTotalCount).text = // 更新歌曲总数文本
                "共 ${list.size} 首"
        }

        initBottomPlayer() // 初始化底部播放栏
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState() // 页面可见同步播放状态
    }

    private fun initBottomPlayer() { // 初始化底部全局播放控件
        val bpBinding = com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding.bind(
            findViewById(R.id.include_bottom_player)
        )
        com.example.netmusicandroid.utils.BottomPlayerBinder.bind(this, this, bpBinding, bottomVm)
    }
}