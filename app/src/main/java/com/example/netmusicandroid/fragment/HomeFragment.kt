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
import com.example.netmusicandroid.viewmodel.MainViewModel
import com.example.netmusicandroid.viewmodel.SingerListViewModel
import com.example.netmusicandroid.viewmodel.UserPlaylistViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var playlistVm: UserPlaylistViewModel
    private lateinit var singerListVm: SingerListViewModel
    private val songRepository = SongRepository()
    private lateinit var songAdapter: HomeSongAdapter
    private lateinit var playlistAdapter: HomePlaylistAdapter
    private lateinit var singerAdapter: SearchSingerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]
        playlistVm = ViewModelProvider(this)[UserPlaylistViewModel::class.java]
        singerListVm = ViewModelProvider(this)[SingerListViewModel::class.java]

        // 热门歌曲
        val rvHomeSongs = view.findViewById<RecyclerView>(R.id.rvHomeSongs)
        songAdapter = HomeSongAdapter(emptyList()) { song ->
            mainViewModel.playSong(song)
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        rvHomeSongs.adapter = songAdapter

        // 我的歌单
        val rvPlaylist = view.findViewById<RecyclerView>(R.id.rvPlaylist)
        playlistAdapter = HomePlaylistAdapter { playlistId ->
            startActivity(Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlistId)
            })
        }
        rvPlaylist.adapter = playlistAdapter

        // 推荐歌手
        val rvArtist = view.findViewById<RecyclerView>(R.id.rvArtist)
        singerAdapter = SearchSingerAdapter { singer ->
            startActivity(Intent(requireContext(), SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            })
        }
        rvArtist.adapter = singerAdapter

        // 搜索框 + 搜索图标 → 跳转搜索页面
        val toSearch = Intent(requireContext(), SearchActivity::class.java)
        view.findViewById<View>(R.id.ll_search_bar).setOnClickListener { startActivity(toSearch) }
        view.findViewById<View>(R.id.btnSearch).setOnClickListener { startActivity(toSearch) }

        // 我的歌单「更多」→ 跳转 PlaylistActivity
        view.findViewById<View>(R.id.tvMorePlaylist).setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }

        // 推荐歌手「更多」→ 跳转歌手列表页
        view.findViewById<View>(R.id.tvMoreArtist).setOnClickListener {
            startActivity(Intent(requireContext(), SingerListActivity::class.java))
        }

        initBottomPlayer(view)
        loadSongs()
        loadPlaylists()
        loadSingers()
    }

    override fun onResume() {
        super.onResume()
        bottomVm.syncPlayState()
    }

    private fun initBottomPlayer(view: View) {
        val bp = LayoutBottomPlayerBinding.bind(view.findViewById(R.id.include_bottom_player))
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
            // 同步 MainViewModel，保持播放页状态一致
            if (mainViewModel.isPlaying.value != playing) {
                mainViewModel.togglePlayState()
            }
        }
        bottomVm.toastMsg.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                bottomVm.clearToast()
            }
        }
        bp.ivPrev.setOnClickListener {
            bottomVm.playPrev()
            syncCurrentSongToMain()
        }
        bp.ivPlayToggle.setOnClickListener {
            bottomVm.togglePlayPause()
            mainViewModel.togglePlayState()
        }
        bp.ivNext.setOnClickListener {
            bottomVm.playNext()
            syncCurrentSongToMain()
        }

        val goPlayer = View.OnClickListener {
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        bp.cvCover.setOnClickListener(goPlayer)
        bp.llSongInfo.setOnClickListener(goPlayer)
    }

    /** 将底部播放栏当前歌曲同步到 MainViewModel，保持播放页一致 */
    private fun syncCurrentSongToMain() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repo = com.example.netmusicandroid.data.repository.PlayQueueRepository()
            val current = repo.getCurrentSong()
            if (current != null) {
                mainViewModel.playSong(
                    com.example.netmusicandroid.data.model.SongDetail(
                        song_id = current.song_id,
                        song_name = current.song_name,
                        singer_name = current.singer_name,
                        play_url = current.play_url,
                        cover_url = current.cover_url,
                        duration = current.duration
                    )
                )
            }
        }
    }

    private fun loadSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                val displayList = (if (list.size > 3) list.take(3) else list).toMutableList()
                // 先展示基本信息（封面用占位图）
                songAdapter.updateData(displayList)

                // 并行获取所有歌曲详情，全部完成后统一更新一次 Adapter
                val details = coroutineScope {
                    displayList.map { song ->
                        async { songRepository.fetchSongDetail(song.song_id) }
                    }.awaitAll()
                }
                // 用详情结果替换列表项，再次提交（只触发一次 DiffUtil + 绑定）
                details.forEachIndexed { index, detailResult ->
                    detailResult.onSuccess { displayList[index] = it }
                }
                songAdapter.updateData(displayList.toList())
            }.onFailure { ex ->
                Toast.makeText(requireContext(), "获取歌曲失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPlaylists() {
        playlistVm.collectionList.observe(viewLifecycleOwner) { list ->
            playlistAdapter.submitList(list)
        }
        playlistVm.loadUserCollection()
    }

    private fun loadSingers() {
        singerListVm.singers.observe(viewLifecycleOwner) { list ->
            singerAdapter.submitList(list)
        }
        singerListVm.loadSingers()
    }
}
