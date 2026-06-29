package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SongListAdapter
import com.example.netmusicandroid.databinding.ActivityRecentPlayBinding
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.RecentPlayViewModel

class RecentPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecentPlayBinding
    private lateinit var viewModel: RecentPlayViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var songAdapter: SongListAdapter

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
        val bp = binding.includeBottomPlayer
        bottomVm.songName.observe(this) { bp.tvSongName.text = it }
        bottomVm.singerName.observe(this) { bp.tvSinger.text = it }
        bottomVm.coverUrl.observe(this) { url ->
            if (!url.isNullOrEmpty()) ImageLoadUtil.loadImage(bp.ivSongCover, url)
        }
        bottomVm.hasCurrentSong.observe(this) { has ->
            bp.root.visibility = if (has) View.VISIBLE else View.GONE
        }
        bottomVm.isPlaying.observe(this) { playing ->
            bp.ivPlayToggle.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
            )
        }
        bottomVm.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                bottomVm.clearToast()
            }
        }
        bp.ivPrev.setOnClickListener { bottomVm.playPrev() }
        bp.ivPlayToggle.setOnClickListener { bottomVm.togglePlayPause() }
        bp.ivNext.setOnClickListener { bottomVm.playNext() }

        val goPlayer = View.OnClickListener { BaseActivity.navigateToPlayerFrom(this) }
        bp.cvCover.setOnClickListener(goPlayer)
        bp.llSongInfo.setOnClickListener(goPlayer)
    }

    private fun initSongRecycler() {
        // 删除回调 → 从播放历史移除该歌曲
        songAdapter = SongListAdapter { songId ->
            viewModel.deleteBySongId(songId)
        }
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
