package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SongListAdapter
import com.example.netmusicandroid.databinding.ActivityPlaylistDetailBinding
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.PlaylistDetailViewModel
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.utils.ToastUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PlaylistDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistDetailBinding
    private lateinit var playlistDetailVm: PlaylistDetailViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepo = SongRepository.getInstance()
    // 歌单ID（从上个页面Intent接收）
    private var targetPlaylistId = -1
    // 歌曲列表Adapter
    private lateinit var songAdapter: SongListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. ViewBinding初始化
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化VM
        initViewModel()
        // 获取上个页面传递的歌单ID
        getIntentPlaylistId()
        // 初始化歌曲RecyclerView & Adapter
        initSongRecycler()
        // 绑定页面所有点击事件
        initAllClick()
        // 观察ViewModel数据
        observeViewModelData()
        // 底部播放栏
        initBottomPlayer()

        // ID合法则加载歌单详情
        if (targetPlaylistId != -1) {
            playlistDetailVm.loadPlaylistDetail(targetPlaylistId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    // 1. 初始化ViewModel
    private fun initViewModel() {
        playlistDetailVm = ViewModelProvider(this)[PlaylistDetailViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
    }

    // ── 底部播放栏 ──────────────────────────────

    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
    }

    // 2. 从Intent取出PlaylistId
    private fun getIntentPlaylistId() {
        targetPlaylistId = intent.getIntExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, -1)
    }

    // 3. 初始化歌曲列表RecyclerView + SongListAdapter
    private fun initSongRecycler() {
        // 实例化Adapter，传入单首删除回调
        songAdapter = SongListAdapter(
            onSongDeleteClick = { deleteSongId ->
                playlistDetailVm.deleteSongInPlaylist(targetPlaylistId, deleteSongId)
            },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id)
                    result.onSuccess { detail ->
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        // 线性布局
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        binding.rvSongList.adapter = songAdapter
    }

    // 4. 监听VM所有LiveData，渲染UI（已修正字段报错）
    private fun observeViewModelData() {
        // 监听歌单头部信息（封面、名称、歌曲总数）
        playlistDetailVm.playlistDetail.observe(this) { detail ->
            // 防御空指针：detail 为 null 时跳过渲染
            if (detail == null) return@observe
            // 歌单封面
            ImageLoadUtil.loadImage(binding.ivPlaylistCover, MusicPlayerManager.resolveUrl(detail.cover_url))
            // 歌单名称
            binding.tvPlaylistName.text = detail.playlist_name
            // 通过songs列表长度计算歌曲总数
            val totalCount = detail.songs.size
            binding.tvSongTotal.text = "$totalCount 首"
            binding.tvPlayAll.text = "播放全部($totalCount)"
            // 提交歌曲列表
            songAdapter.submitList(detail.songs)
        }

        // Toast弹窗提示监听
        playlistDetailVm.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                playlistDetailVm.clearToast()
            }
        }

        // Loading加载状态（依赖布局中id=progressBar控件）
        playlistDetailVm.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    // 5. 页面全部点击事件绑定
    private fun initAllClick() {
        // 顶部返回按钮，关闭当前页面
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 播放全部整行点击
        binding.rvPlayAll.setOnClickListener {
            playlistDetailVm.playAllSong()
        }

        // 右上角批量操作点击
        binding.llBatchOperate.setOnClickListener {
            playlistDetailVm.openBatchOperate()
        }
    }
}