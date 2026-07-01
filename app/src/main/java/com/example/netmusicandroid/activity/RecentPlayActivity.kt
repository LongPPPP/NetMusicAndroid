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
import com.example.netmusicandroid.databinding.ActivityRecentPlayBinding
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.RecentPlayViewModel
import kotlinx.coroutines.launch

class RecentPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecentPlayBinding
    private lateinit var viewModel: RecentPlayViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var songAdapter: SongListAdapter
    private val songRepo = SongRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initSongRecycler()
        initAllClick()
        observeViewModelData()
        initBottomPlayer()
    }

    override fun onResume() {
        super.onResume()
        bottomVm.syncPlayState()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[RecentPlayViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
    }

    // ── 底部播放栏 ──────────────────────────────

    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
    }

    private fun initSongRecycler() {
        // 删除回调 → 从播放历史移除该歌曲
        songAdapter = SongListAdapter(
            onSongDeleteClick = { songId -> viewModel.deleteBySongId(songId) },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id)
                    result.onSuccess { detail ->
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        binding.rvSongList.adapter = songAdapter
    }

    private fun observeViewModelData() {
        // 监听歌曲列表（Room Flow → LiveData，自动刷新）
        viewModel.recentPlaySongs.observe(this) { songList ->
            songAdapter.submitList(songList)

            val count = songList.size
            binding.tvSongCount.text = "$count 首"
            binding.tvPlayAll.text = "播放全部($count)"

            // 空状态切换
            val isEmpty = songList.isEmpty()
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvSongList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        // Toast 弹窗监听
        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                viewModel.clearToast()
            }
        }
    }

    private fun initAllClick() {
        // 返回
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 清空全部播放历史
        binding.tvClearAll.setOnClickListener {
            viewModel.clearAll()
        }

        // 播放全部
        binding.rvPlayAll.setOnClickListener {
            viewModel.playAll()
        }
    }
}
