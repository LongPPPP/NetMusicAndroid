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
import com.example.netmusicandroid.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var songAdapter: SongListAdapter
    private val songRepo = SongRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myfavorites)

        viewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]

        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCollectionList)
        songAdapter = SongListAdapter(
            onSongDeleteClick = { songId -> viewModel.removeFavorite(songId) },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id)
                    result.onSuccess { detail ->
                        bottomVm.playSong(detail)
                    }
                }
            }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = songAdapter

        viewModel.songs.observe(this) { songAdapter.submitList(it) }
        viewModel.playlistName.observe(this) {
            findViewById<android.widget.TextView>(R.id.tvTotalCount).text = it
        }
        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                viewModel.clearToast()
            }
        }
        viewModel.isLoading.observe(this) { loading ->
            findViewById<View>(R.id.progressBar)?.visibility =
                if (loading) View.VISIBLE else View.GONE
        }

        initBottomPlayer()
        viewModel.loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initBottomPlayer() {
        val bpBinding = com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding.bind(
            findViewById(R.id.include_bottom_player)
        )
        com.example.netmusicandroid.utils.BottomPlayerBinder.bind(this, this, bpBinding, bottomVm)
    }
}
