package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SingerSongAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.MainViewModel
import com.example.netmusicandroid.viewmodel.SingerViewModel
import kotlinx.coroutines.launch

class SingerActivity : AppCompatActivity() {

    private lateinit var viewModel: SingerViewModel
    private lateinit var mainViewModel: MainViewModel // 共享播放状态
    private val songRepository = SongRepository()
    private lateinit var adapter: SingerSongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_singer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val singerName = intent.getStringExtra("SINGER_NAME") ?: ""
        
        // 初始化共享 ViewModel (用于控制播放)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val tvName = findViewById<TextView>(R.id.tvSingerName)
        val tvSignature = findViewById<TextView>(R.id.tvSingerSignature)
        val ivAvatar = findViewById<ImageView>(R.id.ivSingerAvatar)
        val rvSongs = findViewById<RecyclerView>(R.id.rvSingerSongs)
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 设置作品列表适配器
        adapter = SingerSongAdapter(emptyList()) { hotSong ->
            // 点击作品：获取详情并播放
            playSingerSong(hotSong.song_id)
        }
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = adapter

        viewModel = ViewModelProvider(this)[SingerViewModel::class.java]
        
        viewModel.singerDetail.observe(this) { detail ->
            if (detail != null) {
                tvName.text = detail.singer_name
                tvSignature.text = detail.description ?: "该歌手暂无简介"
                
                val avatarUrl = if (detail.avatar_url?.startsWith("http") == true) {
                    detail.avatar_url
                } else {
                    "http://10.240.200.130:3000${detail.avatar_url}"
                }
                Glide.with(this).load(avatarUrl).placeholder(R.drawable.music).into(ivAvatar)
                
                // 更新歌曲列表数据
                adapter.updateData(detail.hot_songs)
            }
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.loadSingerByName(singerName)
    }

    private fun playSingerSong(songId: Int) {
        lifecycleScope.launch {
            val result = songRepository.fetchSongDetail(songId)
            result.onSuccess { songDetail ->
                // 全局播放
                mainViewModel.playSong(songDetail)
                // 提示并返回
                Toast.makeText(this@SingerActivity, "正在播放: ${songDetail.song_name}", Toast.LENGTH_SHORT).show()
                finish() // 听歌时关闭详情页，回到播放页（或者你可以选择不关闭）
            }.onFailure {
                Toast.makeText(this@SingerActivity, "获取歌曲详情失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
