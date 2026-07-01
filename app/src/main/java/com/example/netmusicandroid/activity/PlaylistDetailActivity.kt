package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.adapter.SongListAdapter
import com.example.netmusicandroid.data.model.PlaylistDetailData
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.ActivityPlaylistDetailBinding
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.base.BaseBindingActivity
import com.example.netmusicandroid.ui.state.PlaylistDetailUiState
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.PlaylistDetailViewModel
import kotlinx.coroutines.launch

class PlaylistDetailActivity : BaseBindingActivity<ActivityPlaylistDetailBinding>() {
    private lateinit var playlistDetailVm: PlaylistDetailViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepo = SongRepository.getInstance()
    private var targetPlaylistId = -1
    private lateinit var songAdapter: SongListAdapter

    override fun inflateBinding(inflater: LayoutInflater): ActivityPlaylistDetailBinding {
        return ActivityPlaylistDetailBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        getIntentPlaylistId()
        initSongRecycler()
        initAllClick()
        observeViewModelData()
        initBottomPlayer()

        if (targetPlaylistId != -1) {
            playlistDetailVm.loadPlaylistDetail(targetPlaylistId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initViewModel() {
        playlistDetailVm = ViewModelProvider(this)[PlaylistDetailViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
    }

    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
    }

    private fun getIntentPlaylistId() {
        targetPlaylistId = intent.getIntExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, -1)
    }

    private fun initSongRecycler() {
        songAdapter = SongListAdapter(
            onSongDeleteClick = { deleteSongId ->
                playlistDetailVm.deleteSongInPlaylist(targetPlaylistId, deleteSongId)
            },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    songRepo.fetchSongDetail(songItem.song_id).onSuccess { detail ->
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        binding.rvSongList.layoutManager = LinearLayoutManager(this)
        binding.rvSongList.adapter = songAdapter
    }

    private fun observeViewModelData() {
        playlistDetailVm.uiState.observe(this) { render(it) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playlistDetailVm.events.collect { event ->
                    when (event) {
                        is UiEvent.Toast -> ToastUtil.showShort(event.message)
                    }
                }
            }
        }
    }

    private fun render(state: PlaylistDetailUiState) {
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        state.playlistDetail?.let { renderPlaylistDetail(it) }
    }

    private fun renderPlaylistDetail(detail: PlaylistDetailData) {
        ImageLoadUtil.loadImage(binding.ivPlaylistCover, MusicPlayerManager.resolveUrl(detail.cover_url))
        binding.tvPlaylistName.text = detail.playlist_name
        val totalCount = detail.songs.size
        binding.tvSongTotal.text = "$totalCount 首"
        binding.tvPlayAll.text = "播放全部($totalCount)"
        songAdapter.submitList(detail.songs)
    }

    private fun initAllClick() {
        binding.ivBack.setOnClickListener { finish() }
        binding.rvPlayAll.setOnClickListener { playlistDetailVm.playAllSong() }
        binding.llBatchOperate.setOnClickListener { playlistDetailVm.openBatchOperate() }
    }
}
