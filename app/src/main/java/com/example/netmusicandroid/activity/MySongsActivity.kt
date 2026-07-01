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
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.MySongsViewModel
import kotlinx.coroutines.launch

/**
 * 歌手-我的作品页面
 * 展示当前登录歌手上传的所有歌曲，支持点击播放，包含底部全局播放栏
 */
class MySongsActivity : AppCompatActivity() {
    // 当前页面VM：管理歌手作品数据
    private lateinit var viewModel: MySongsViewModel
    // 底部播放栏、全局播放器共享VM
    private lateinit var bottomVm: BottomPlayerViewModel
    // 歌曲列表适配器
    private lateinit var songAdapter: SongListAdapter
    // 歌曲数据仓库，请求歌曲详情
    private val songRepo = SongRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mysongs)

        // 初始化各个ViewModel
        viewModel = ViewModelProvider(this)[MySongsViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]

        // 返回按钮关闭页面
        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }

        // 初始化歌曲列表RecyclerView
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCollectionList)
        songAdapter = SongListAdapter(
            // 删除歌曲逻辑暂未实现
            onSongDeleteClick = { },
            // 点击歌曲条目：请求完整歌曲详情并播放
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id)
                    result.onSuccess { detail ->
                        // 更新全局播放歌曲并播放
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = songAdapter

        // 监听歌手作品列表数据，转换为UI展示模型刷新列表
        viewModel.songs.observe(this) { list ->
            songAdapter.submitList(list.map { entity ->
                com.example.netmusicandroid.data.model.SongItem(
                    song_id = entity.song_id,
                    song_name = entity.song_name,
                    singer_id = 0,
                    singer_name = entity.singer_name,
                    cover_url = entity.cover_url,
                    play_url = entity.play_url ?: "",
                    duration = entity.duration ?: 0,
                    added_at = ""
                )
            })
        }

        // 监听弹窗提示消息，展示后清空
        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                viewModel.clearToast()
            }
        }

        initBottomPlayer() // 初始化底部播放控制器
        viewModel.loadMySongs() // 请求加载歌手自己上传的歌曲
    }

    override fun onResume() {
        super.onResume()
        // 页面回到前台时同步播放状态，避免UI图标错乱
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    /**
     * 初始化页面底部全局播放栏
     * 绑定播放数据、切歌按钮、跳转全屏播放器逻辑
     */
    private fun initBottomPlayer() {
        val bpBinding = com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding.bind(
            findViewById(R.id.include_bottom_player)
        )
        com.example.netmusicandroid.utils.BottomPlayerBinder.bind(this, this, bpBinding, bottomVm)
    }
}