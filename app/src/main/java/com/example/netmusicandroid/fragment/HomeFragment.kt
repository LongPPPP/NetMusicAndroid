package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.activity.PlaylistDetailActivity
import com.example.netmusicandroid.activity.MoreSongActivity
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.activity.SingerListActivity
import com.example.netmusicandroid.adapter.HomePlaylistAdapter
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.adapter.SearchSingerAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SingerListViewModel
import com.example.netmusicandroid.viewmodel.UserPlaylistViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 首页Fragment
 * 展示热门歌曲、我的收藏歌单、推荐歌手三大板块，包含底部全局播放控制器
 */
class HomeFragment : Fragment() {
    // 全局共享VM
    private lateinit var bottomVm: BottomPlayerViewModel
    // 页面独立VM：用户歌单、歌手列表
    private lateinit var playlistVm: UserPlaylistViewModel
    private lateinit var singerListVm: SingerListViewModel
    // 歌曲数据仓库，请求网络歌曲
    private val songRepository = SongRepository()
    // 三个RecyclerView适配器
    private lateinit var songAdapter: HomeSongAdapter
    private lateinit var playlistAdapter: HomePlaylistAdapter
    private lateinit var singerAdapter: SearchSingerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载首页布局
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化所有ViewModel
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]
        playlistVm = ViewModelProvider(this)[UserPlaylistViewModel::class.java]
        singerListVm = ViewModelProvider(this)[SingerListViewModel::class.java]

// 1. 热门歌曲列表RecyclerView初始化
        val rvHomeSongs = view.findViewById<RecyclerView>(R.id.rvHomeSongs)
        songAdapter = HomeSongAdapter(emptyList()) { song ->
            // 点击歌曲仅触发播放，不跳转全屏播放页
            bottomVm.playSong(song)
        }
        rvHomeSongs.adapter = songAdapter

        // 2. 我的歌单RecyclerView初始化
        val rvPlaylist = view.findViewById<RecyclerView>(R.id.rvPlaylist)
        playlistAdapter = HomePlaylistAdapter { playlistId ->
            // 传入歌单ID，跳转歌单详情页
            startActivity(Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlistId)
            })
        }
        rvPlaylist.adapter = playlistAdapter

        // 3. 推荐歌手RecyclerView初始化
        val rvArtist = view.findViewById<RecyclerView>(R.id.rvArtist)
        singerAdapter = SearchSingerAdapter { singer ->
            // 传入歌手名，跳转歌手主页
            startActivity(Intent(requireContext(), SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            })
        }
        rvArtist.adapter = singerAdapter

        // 搜索栏、搜索按钮点击跳转搜索页面
        val toSearch = Intent(requireContext(), SearchActivity::class.java)
        view.findViewById<View>(R.id.ll_search_bar).setOnClickListener { startActivity(toSearch) }
        view.findViewById<View>(R.id.btnSearch).setOnClickListener { startActivity(toSearch) }

        // 热门歌曲更多按钮 → 歌曲分页列表页
        view.findViewById<View>(R.id.tvMoreSong).setOnClickListener {
            startActivity(Intent(requireContext(), MoreSongActivity::class.java))
        }

        // 歌单更多按钮 → 全部收藏歌单页面
        view.findViewById<View>(R.id.tvMorePlaylist).setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }

        // 歌手更多按钮 → 全部歌手列表页面
        view.findViewById<View>(R.id.tvMoreArtist).setOnClickListener {
            startActivity(Intent(requireContext(), SingerListActivity::class.java))
        }

        initBottomPlayer(view)  // 初始化底部播放栏
        loadSongs()             // 加载首页热门歌曲
        loadPlaylists()         // 加载用户收藏歌单
        loadSingers()           // 加载推荐歌手列表
    }

    override fun onResume() {
        super.onResume()
        // 页面可见时同步播放器播放状态
        bottomVm.syncPlayState()
    }

    /**
     * 初始化底部全局播放控件
     * 绑定播放数据、切歌按钮、跳转全屏播放器逻辑
     */
    private fun initBottomPlayer(view: View) {
        val bp = LayoutBottomPlayerBinding.bind(view.findViewById(R.id.include_bottom_player))

        // 全部改用 viewLifecycleOwner，视图销毁自动解绑，避免后台回调崩溃
        bottomVm.songName.observe(viewLifecycleOwner) { bp.tvSongName.text = it }
        bottomVm.singerName.observe(viewLifecycleOwner) { bp.tvSinger.text = it }
        bottomVm.coverUrl.observe(viewLifecycleOwner) { url ->
            ImageLoadUtil.loadImage(bp.ivSongCover, MusicPlayerManager.resolveUrl(url))
        }
        bottomVm.hasCurrentSong.observe(viewLifecycleOwner) { has ->
            bp.root.visibility = if (has) View.VISIBLE else View.GONE
        }
        bottomVm.isPlaying.observe(viewLifecycleOwner) { playing ->
            bp.ivPlayToggle.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
            )
        }
        // Toast 增加空安全兜底，上下文为空时直接跳过不弹
        bottomVm.toastMsg.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                context?.let { Toast.makeText(it, msg, Toast.LENGTH_SHORT).show() }
                bottomVm.clearToast()
            }
        }

        bp.ivPrev.setOnClickListener { bottomVm.playPrev() }
        bp.ivPlayToggle.setOnClickListener { bottomVm.togglePlayPause() }
        bp.ivNext.setOnClickListener { bottomVm.playNext() }

        val goPlayer = View.OnClickListener {
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        bp.cvCover.setOnClickListener(goPlayer)
        bp.llSongInfo.setOnClickListener(goPlayer)
    }

    /**
     * 加载首页热门歌曲（最多展示3首）
     * 先展示简易列表，再并行请求完整歌曲详情，统一刷新UI
     */
    private fun loadSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                // 截取前3首展示
                val displayList = (if (list.size > 3) list.take(3) else list).toMutableList()
                songAdapter.updateData(displayList)

                // 并行并发请求每首歌完整详情
                val details = coroutineScope {
                    displayList.map { song ->
                        async { songRepository.fetchSongDetail(song.song_id) }
                    }.awaitAll()
                }
                // 用详情替换原有简易数据，一次性更新Adapter
                details.forEachIndexed { index, detailResult ->
                    detailResult.onSuccess { displayList[index] = it }
                }
                songAdapter.updateData(displayList.toList())
            }.onFailure { ex ->
                context?.let {
                    Toast.makeText(it, "获取歌曲失败: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 加载用户收藏歌单，监听数据自动更新列表
     */
    private fun loadPlaylists() {
        playlistVm.collectionList.observe(viewLifecycleOwner) { list ->
            playlistAdapter.submitList(list)
        }
        playlistVm.loadUserCollection()
    }

    /**
     * 加载推荐歌手列表，监听数据自动更新列表
     */
    private fun loadSingers() {
        singerListVm.singers.observe(viewLifecycleOwner) { list ->
            singerAdapter.submitList(list)
        }
        singerListVm.loadSingers()
    }
}