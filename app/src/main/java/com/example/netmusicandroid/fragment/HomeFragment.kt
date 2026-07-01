package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.activity.MoreSongActivity
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.activity.PlaylistDetailActivity
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.activity.SingerListActivity
import com.example.netmusicandroid.adapter.HomePlaylistAdapter
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.adapter.SearchSingerAdapter
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.FragmentHomeBinding
import com.example.netmusicandroid.ui.base.BaseBindingFragment
import com.example.netmusicandroid.ui.state.HomeUiState
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ToastUtil
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
class HomeFragment : BaseBindingFragment<FragmentHomeBinding>() {
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var playlistVm: UserPlaylistViewModel
    private lateinit var singerListVm: SingerListViewModel
    private val songRepository = SongRepository.getInstance()
    private lateinit var songAdapter: HomeSongAdapter
    private lateinit var playlistAdapter: HomePlaylistAdapter
    private lateinit var singerAdapter: SearchSingerAdapter
    private var homeUiState = HomeUiState()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]
        playlistVm = ViewModelProvider(this)[UserPlaylistViewModel::class.java]
        singerListVm = ViewModelProvider(this)[SingerListViewModel::class.java]

        initAdapters()
        initClickListeners()
        initBottomPlayer()
        loadSongs()
        loadPlaylists()
        loadSingers()
    }

    override fun onResume() {
        super.onResume()
        bottomVm.syncPlayState()
    }

    private fun initAdapters() {
        songAdapter = HomeSongAdapter { song ->
            bottomVm.playSong(song)
        }
        binding.rvHomeSongs.adapter = songAdapter

        playlistAdapter = HomePlaylistAdapter { playlistId ->
            startActivity(Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlistId)
            })
        }
        binding.rvPlaylist.adapter = playlistAdapter

        singerAdapter = SearchSingerAdapter { singer ->
            startActivity(Intent(requireContext(), SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            })
        }
        binding.rvArtist.adapter = singerAdapter
    }

    private fun initClickListeners() {
        val toSearch = Intent(requireContext(), SearchActivity::class.java)
        binding.llSearchBar.setOnClickListener { startActivity(toSearch) }
        binding.btnSearch.setOnClickListener { startActivity(toSearch) }
        binding.tvMoreSong.setOnClickListener {
            startActivity(Intent(requireContext(), MoreSongActivity::class.java))
        }
        binding.tvMorePlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }
        binding.tvMoreArtist.setOnClickListener {
            startActivity(Intent(requireContext(), SingerListActivity::class.java))
        }
    }

    /**
     * 初始化底部全局播放控件
     * 绑定播放数据、切歌按钮、跳转全屏播放器逻辑
     */
    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(
            viewLifecycleOwner,
            requireContext(),
            binding.includeBottomPlayer,
            bottomVm,
            onOpenPlayer = { (requireActivity() as BaseActivity).navigateToPlayer() }
        )
    }

    /**
     * 加载首页热门歌曲，等详情请求完成后一次性提交列表，减少列表闪烁。
     */
    private fun loadSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            updateHomeState { copy(isLoadingSongs = true, songError = null) }
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                val displayList = if (list.size > 3) list.take(3) else list
                val details = coroutineScope {
                    displayList.map { song ->
                        async { songRepository.fetchSongDetail(song.song_id) }
                    }.awaitAll()
                }
                val detailedSongs = details.mapIndexedNotNull { index, detailResult ->
                    detailResult.getOrNull() ?: displayList.getOrNull(index)
                }
                if (detailedSongs.isNotEmpty()) {
                    renderHomeState(homeUiState.copy(isLoadingSongs = false, songs = detailedSongs))
                } else {
                    val message = "获取歌曲失败"
                    updateHomeState { copy(isLoadingSongs = false, songError = message) }
                    ToastUtil.showShort(message)
                }
            }.onFailure { ex ->
                val message = "获取歌曲失败: ${ex.message}"
                updateHomeState { copy(isLoadingSongs = false, songError = message) }
                ToastUtil.showShort(message)
            }
        }
    }

    private fun renderHomeState(state: HomeUiState) {
        homeUiState = state
        songAdapter.submitSongs(state.songs)
    }

    private fun updateHomeState(reducer: HomeUiState.() -> HomeUiState) {
        renderHomeState(homeUiState.reducer())
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
