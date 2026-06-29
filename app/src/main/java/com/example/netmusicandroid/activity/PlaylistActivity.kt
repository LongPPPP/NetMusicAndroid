package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.UserPlaylistAdapter
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.databinding.ActivityPlaylistBinding
import com.example.netmusicandroid.dialog.CreatePlaylistDialog
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.UserPlaylistViewModel
import com.example.netmusicandroid.utils.ToastUtil
import kotlinx.coroutines.launch

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var collectionVm: UserPlaylistViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var collectionAdapter: UserPlaylistAdapter

    // 常量：传递歌单ID的key
    companion object {
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initAdapter()
        initObserver()
        initClick()
        initBottomPlayer()
        // 页面加载自动请求全部歌单
        collectionVm.loadUserCollection()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initViewModel() {
        collectionVm = ViewModelProvider(this)[UserPlaylistViewModel::class.java]
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

    private fun initAdapter() {
        // 第一个lambda：条目点击回调，跳转歌单详情
        // 第二个lambda：删除按钮回调
        collectionAdapter = UserPlaylistAdapter(
            onItemClick = { playlistId ->
                // 构建跳转意图，携带歌单ID
                val intent = Intent(this, PlaylistDetailActivity::class.java).apply {
                    putExtra(EXTRA_PLAYLIST_ID, playlistId)
                }
                startActivity(intent)
            },
            onDeleteClick = { targetCollectionId ->
                collectionVm.deleteUserPlaylist(targetCollectionId)
            }
        )
        binding.rvCollectionList.layoutManager = LinearLayoutManager(this)
        binding.rvCollectionList.adapter = collectionAdapter
    }

    private fun initObserver() {
        // 监听歌单列表刷新UI
        collectionVm.collectionList.observe(this) { list ->
            collectionAdapter.submitList(list)
            binding.tvTotalCount.text = "共 ${list.size} 个歌单"
        }
        // 弹窗提示（删除成功/失败、网络异常）
        collectionVm.toastMsg.observe(this) { msg ->
            ToastUtil.showShort(msg)
        }
    }

    private fun initClick() {
        // 返回我的页面
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 新建歌单：弹出独立 Dialog，通过回调调用 ViewModel
        binding.llCreateUserPlaylist.setOnClickListener {
            CreatePlaylistDialog(this) { playlistName ->
                collectionVm.createUserPlaylist(playlistName)
            }.show()
        }
    }
}