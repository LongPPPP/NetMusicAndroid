package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.CommentActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.constant.ApiConst
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val songRepository = SongRepository()
    private var currentSongId: Int = -1 
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val tvSongName = view.findViewById<TextView>(R.id.tvSongName)
        val tvSinger = view.findViewById<TextView>(R.id.tvSinger)
        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvCurrentTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvTotalTime = view.findViewById<TextView>(R.id.tvTotalTime)

        // 【极致同步】：抢在异步加载之前，先尝试同步一次进度条
        val currentDuration = MusicPlayerManager.getDuration()
        if (currentDuration > 0) {
            seekBar.max = currentDuration
            seekBar.progress = MusicPlayerManager.getCurrentPosition()
            tvTotalTime.text = formatTime(currentDuration)
            tvCurrentTime.text = formatTime(MusicPlayerManager.getCurrentPosition())
        }

        val baseHost = ApiConst.BASE_URL.replace("/api/v1/", "")

        // 观察当前播放的歌曲
        mainViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                val isSameSong = currentSongId == song.song_id
                currentSongId = song.song_id
                tvSongName.text = song.song_name
                tvSinger.text = song.singer_name
                
                // 加载详情（无论是否是同一首歌，都要加载以补全 UI）
                loadSongDetail(song.song_id, baseHost, imgCover, tvTotalTime, seekBar)
            }
        }

        // 观察播放状态，控制图标
        mainViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            // 如果 isPlaying 为 true，说明正在播放，应该显示“暂停”图标 (||) 让用户点
            // 如果 isPlaying 为 false，说明已暂停，应该显示“播放”图标 (>) 让用户点
            if (isPlaying) {
                btnPlay.setImageResource(R.drawable.pause_button)
                handler.post(updateProgressTask) 
            } else {
                btnPlay.setImageResource(R.drawable.play_button)
                handler.removeCallbacks(updateProgressTask)
            }
        }

        btnPlay.setOnClickListener {
            if (currentSongId == -1) return@setOnClickListener
            MusicPlayerManager.toggle()
            mainViewModel.togglePlayState()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { handler.removeCallbacks(updateProgressTask) }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { MusicPlayerManager.seekTo(it.progress) }
                if (MusicPlayerManager.isPlaying()) handler.post(updateProgressTask)
            }
        })

        view.findViewById<ImageButton>(R.id.btnComment).setOnClickListener {
            if (currentSongId == -1) return@setOnClickListener
            val intent = Intent(requireContext(), CommentActivity::class.java)
            intent.putExtra("SONG_ID", currentSongId)
            startActivity(intent)
        }

        tvSinger.setOnClickListener {
            val name = tvSinger.text.toString()
            if (name.isNotEmpty() && name != "歌手") {
                val intent = Intent(requireContext(), SingerActivity::class.java)
                intent.putExtra("SINGER_NAME", name)
                startActivity(intent)
            }
        }
    }

    private fun loadSongDetail(songId: Int, host: String, img: ImageView, tvTotal: TextView, sb: SeekBar) {
        lifecycleScope.launch {
            val result = songRepository.fetchSongDetail(songId)
            result.onSuccess { detail ->
                // 1. 同步封面
                val coverUrl = if (detail.cover_url?.startsWith("http") == true) detail.cover_url 
                               else "$host${detail.cover_url}".replace(" ", "%20")
                Glide.with(this@PlayerFragment).load(coverUrl).placeholder(R.drawable.disk).into(img)

                // 2. 处理播放器逻辑
                val playUrl = if (detail.play_url?.startsWith("http") == true) detail.play_url 
                              else "$host${detail.play_url}".replace(" ", "%20")
                
                MusicPlayerManager.onPrepared = { duration ->
                    sb.max = duration
                    tvTotal.text = formatTime(duration)
                    handler.post(updateProgressTask)
                }
                // 播放完成回调由 BottomPlayerViewModel 统一管理（记录历史 + 自动切歌）

                // 执行 play，只有是新歌时才会触发重播
                val isNewPlay = MusicPlayerManager.play(playUrl)
                
                // 3. 【核心修复】：如果不是新歌（说明是切回来的），手动强制同步 UI 状态
                if (!isNewPlay) {
                    val totalMs = MusicPlayerManager.getDuration()
                    if (totalMs > 0) {
                        sb.max = totalMs
                        tvTotal.text = formatTime(totalMs)
                        updateProgressBar()
                    }
                }
            }
        }
    }

    private fun updateProgressBar() {
        val current = MusicPlayerManager.getCurrentPosition()
        view?.let { root ->
            root.findViewById<SeekBar>(R.id.seekBar)?.progress = current
            root.findViewById<TextView>(R.id.tvCurrentTime)?.text = formatTime(current)
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    override fun onResume() {
        super.onResume()
        // 强制同步播放按钮图标，防止 UI 状态错乱
        mainViewModel.currentSong.value?.let {
            // 根据底层播放器的真实状态，校准按钮和定时器
            if (MusicPlayerManager.isPlaying()) {
                handler.post(updateProgressTask)
            } else {
                updateProgressBar()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressTask)
    }
}
