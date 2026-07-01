package com.example.netmusicandroid.activity

import android.os.Bundle
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
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SingerViewModel
import kotlinx.coroutines.launch

class SingerActivity : AppCompatActivity() {

    private lateinit var viewModel: SingerViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepository = SongRepository.getInstance()
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

        val singerId = intent.getIntExtra("SINGER_ID", -1)
        val singerName = intent.getStringExtra("SINGER_NAME") ?: ""

        val tvName = findViewById<TextView>(R.id.tvSingerName)
        val tvSignature = findViewById<TextView>(R.id.tvSingerSignature)
        val ivAvatar = findViewById<ImageView>(R.id.ivSingerAvatar)
        val rvSongs = findViewById<RecyclerView>(R.id.rvSingerSongs)
        
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // 适配器逻辑
        adapter = SingerSongAdapter(
            emptyList(),
            onItemClick = { hotSong -> playSingerSong(hotSong.song_id) },
            onDeleteClick = { hotSong -> deleteSong(hotSong.song_id) } // 下架
        )
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = adapter

        viewModel = ViewModelProvider(this)[SingerViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
        viewModel.singerDetail.observe(this) { detail ->
            if (detail != null) {
                tvName.text = detail.singer_name
                tvSignature.text = detail.description ?: "该歌手暂无简介"
                
                // 动态计算基础地址
                val baseHost = ApiConst.BASE_URL.replace("/api/v1/", "")
                val avatarUrl = if (detail.avatar_url?.startsWith("http") == true) detail.avatar_url 
                               else "$baseHost${detail.avatar_url}"
                
                // 使用 Glide 加载圆形头像
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.music)
                    .circleCrop() // 代码实现圆形效果
                    .into(ivAvatar)
                    
                adapter.updateData(detail.hot_songs)
            }
        }

        // 优先使用歌手ID直接加载；兼容旧版通过名字加载的调用方式
        if (singerId > 0) {
            viewModel.loadSingerById(singerId)
        } else {
            viewModel.loadSingerByName(singerName)
        }
    }

    private fun deleteSong(songId: Int) {
        lifecycleScope.launch {
            val result = songRepository.removeSong(songId)
            result.onSuccess {
                Toast.makeText(this@SingerActivity, "下架成功", Toast.LENGTH_SHORT).show()

                // 【核心修复】：直接对比播放器单例中的全局 ID
                if (com.example.netmusicandroid.utils.MusicPlayerManager.getCurrentSongId() == songId) {
                    com.example.netmusicandroid.utils.MusicPlayerManager.stop() 
                    bottomVm.playSong(null)
                }

                // 刷新页面
                val name = findViewById<TextView>(R.id.tvSingerName).text.toString()
                viewModel.loadSingerByName(name)
            }.onFailure {
                Toast.makeText(this@SingerActivity, "下架失败: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun playSingerSong(songId: Int) {
//        lifecycleScope.launch {
//            val result = songRepository.fetchSongDetail(songId)
//            result.onSuccess { songDetail ->
//                bottomVm.playSong(songDetail)
//                finish()
//            }
//        }
//    }
    private fun playSingerSong(songId: Int) {
        lifecycleScope.launch {
            val result = songRepository.fetchSongDetail(songId)
            result.onSuccess { songDetail ->
                // 统一通过 BottomPlayerViewModel 写入播放状态，避免绕过队列/最近播放记录
                bottomVm.playSong(songDetail)

                // 删掉 finish()，点击后页面不用自动跳转
                Toast.makeText(this@SingerActivity, "正在播放: ${songDetail.song_name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
