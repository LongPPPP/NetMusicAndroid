package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SongListAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.ActivityMyfavoritesBinding
import com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.base.BaseBindingActivity
import com.example.netmusicandroid.ui.state.FavoritesUiState
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

class FavoritesActivity : BaseBindingActivity<ActivityMyfavoritesBinding>() {

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var songAdapter: SongListAdapter
    private val songRepo = SongRepository.getInstance()

    override fun inflateBinding(inflater: LayoutInflater): ActivityMyfavoritesBinding {
        return ActivityMyfavoritesBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]

        binding.ivBack.setOnClickListener { finish() }
        initSongList()
        observeViewModel()
        initBottomPlayer()
        viewModel.loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initSongList() {
        songAdapter = SongListAdapter(
            onSongDeleteClick = { songId -> viewModel.removeFavorite(songId) },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    songRepo.fetchSongDetail(songItem.song_id).onSuccess { detail ->
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        binding.rvCollectionList.layoutManager = LinearLayoutManager(this)
        binding.rvCollectionList.adapter = songAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { render(it) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is UiEvent.Toast -> ToastUtil.showShort(event.message)
                    }
                }
            }
        }
    }

    private fun render(state: FavoritesUiState) {
        songAdapter.submitList(state.songs)
        binding.tvTotalCount.text = state.playlistName
        findViewById<View>(R.id.progressBar)?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
    }

    private fun initBottomPlayer() {
        val bpBinding = LayoutBottomPlayerBinding.bind(findViewById(R.id.include_bottom_player))
        BottomPlayerBinder.bind(this, this, bpBinding, bottomVm)
    }
}
