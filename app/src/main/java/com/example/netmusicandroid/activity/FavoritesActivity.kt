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
    private val songRepo = SongRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myfavorites)

        viewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]

        findViewById<View>(R.id.ivBack).setOnClickListener { finish() }

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvCollectionList)
        songAdapter = SongListAdapter(
            onSongDeleteClick = { },
            onSongClick = { songItem ->
                lifecycleScope.launch {
                    val result = songRepo.fetchSongDetail(songItem.song_id)
                    result.onSuccess { detail ->
                        bottomVm.playSong(detail)
                        MusicPlayerManager.play(
                            MusicPlayerManager.resolveUrl(detail.play_url) ?: return@onSuccess,
                            detail.song_id
                        )
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
        val bp = findViewById<View>(R.id.include_bottom_player)
        val bpBinding = com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding.bind(bp)
        bottomVm.songName.observe(this) { bpBinding.tvSongName.text = it }
        bottomVm.singerName.observe(this) { bpBinding.tvSinger.text = it }
        bottomVm.coverUrl.observe(this) { url ->
            ImageLoadUtil.loadImage(bpBinding.ivSongCover, MusicPlayerManager.resolveUrl(url))
        }
        bottomVm.hasCurrentSong.observe(this) { has ->
            bpBinding.root.visibility = if (has) View.VISIBLE else View.GONE
        }
        bottomVm.isPlaying.observe(this) { playing ->
            bpBinding.ivPlayToggle.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
            )
        }
        bpBinding.ivPrev.setOnClickListener { bottomVm.playPrev() }
        bpBinding.ivPlayToggle.setOnClickListener { bottomVm.togglePlayPause() }
        bpBinding.ivNext.setOnClickListener { bottomVm.playNext() }
        val goPlayer = View.OnClickListener { BaseActivity.navigateToPlayerFrom(this) }
        bpBinding.cvCover.setOnClickListener(goPlayer)
        bpBinding.llSongInfo.setOnClickListener(goPlayer)
    }
}
