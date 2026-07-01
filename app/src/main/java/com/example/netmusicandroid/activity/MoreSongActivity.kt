package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.MoreSongViewModel
import kotlinx.coroutines.launch

class MoreSongActivity : AppCompatActivity() {

    private lateinit var viewModel: MoreSongViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private lateinit var songAdapter: HomeSongAdapter
    private val songRepo = SongRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_song)

        viewModel = ViewModelProvider(this)[MoreSongViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]

        // 返回按钮
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // 设置热门歌曲标题信息
        findViewById<TextView>(R.id.tv_playlist_name).text = "热门歌曲"
        findViewById<TextView>(R.id.tv_song_total).text = "加载中..."

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rv_song_list)
        songAdapter = HomeSongAdapter(emptyList()) { song ->
            playSong(song)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = songAdapter

        // 滚动到底部自动加载更多
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItems = songAdapter.itemCount
                if (lastVisible >= totalItems - 3 && totalItems > 0) {
                    viewModel.loadNextPage()
                }
            }
        })

        // 播放全部
        findViewById<View>(R.id.rv_play_all).setOnClickListener {
            val songs = viewModel.songs.value ?: emptyList()
            if (songs.isNotEmpty()) {
                playSong(songs.first())
            }
        }

        // 数据观察
        viewModel.songs.observe(this) { list ->
            songAdapter.updateData(list)
            findViewById<TextView>(R.id.tv_song_total).text = "${list.size} 首"
            findViewById<TextView>(R.id.tv_play_all).text = "播放全部(${list.size})"
            // 封面使用第一首歌曲的封面
            if (list.isNotEmpty()) {
                val coverUrl = list.first().cover_url
                val baseHost = com.example.netmusicandroid.constant.ApiConst.BASE_URL
                    .replace("/api/v1/", "")
                val url = if (coverUrl.isNullOrEmpty()) null
                else if (coverUrl.startsWith("http")) coverUrl
                else "$baseHost${if (coverUrl.startsWith("/")) coverUrl else "/$coverUrl"}"
                Glide.with(this@MoreSongActivity)
                    .load(url)
                    .placeholder(R.drawable.ic_default_cover)
                    .error(R.drawable.ic_default_cover)
                    .into(findViewById(R.id.iv_playlist_cover))
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            findViewById<View>(R.id.progressBar)?.visibility =
                if (loading && songAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        viewModel.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }

        initBottomPlayer()
        viewModel.loadFirstPage()
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

    private fun playSong(song: SongDetail) {
        lifecycleScope.launch {
            val result = songRepo.fetchSongDetail(song.song_id)
            result.onSuccess { detail ->
                bottomVm.playSong(detail)
            }
        }
    }
}
