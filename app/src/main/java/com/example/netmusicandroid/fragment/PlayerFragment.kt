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
import android.media.MediaPlayer
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.CommentActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.MainViewModel
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepository = SongRepository()
    
    // 初始化为当前正在播放的 ID，防止 Fragment 重建导致的逻辑误触发
    private var currentSongId: Int = -1

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 共享 Activity 级别的 ViewModel
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]

        // Fragment 创建时立刻同步 ID，防止误判
        currentSongId = MusicPlayerManager.getCurrentSongId()

        val tvSongName = view.findViewById<TextView>(R.id.tvSongName)
        val tvSinger = view.findViewById<TextView>(R.id.tvSinger)
        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvCurrentTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvTotalTime = view.findViewById<TextView>(R.id.tvTotalTime)

        // 立即尝试同步已有进度
        val currentDuration = MusicPlayerManager.getDuration()
        if (currentDuration > 0) {
            seekBar.max = currentDuration
            seekBar.progress = MusicPlayerManager.getCurrentPosition()
            tvTotalTime.text = formatTime(currentDuration)
            tvCurrentTime.text = formatTime(MusicPlayerManager.getCurrentPosition())
        }

        // 观察歌曲变化
        mainViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                val isNewSong = currentSongId != song.song_id
                currentSongId = song.song_id

                tvSongName.text = song.song_name
                tvSinger.text = song.singer_name

                if (isNewSong) {
                    loadSongDetail(song.song_id, imgCover, tvTotalTime, seekBar)
                } else {
                    // 修复：同一首歌切回来，也要把封面加载出来
                    val coverUrl = MusicPlayerManager.resolveUrl(song.cover_url) ?: song.cover_url
                    Glide.with(this@PlayerFragment)
                        .load(coverUrl)
                        .placeholder(R.drawable.disk)
                        .into(imgCover)

                    // 同步 UI 即可
                    val duration = MusicPlayerManager.getDuration()
                    if (duration > 0) {
                        seekBar.max = duration
                        tvTotalTime.text = formatTime(duration)
                        updateProgressBar()
                    }
                }
            } else {
                // UI 清空
                currentSongId = -1
                tvSongName.text = "暂无播放歌曲"
                tvSinger.text = "---"
                imgCover.setImageResource(R.drawable.disk)
                seekBar.progress = 0
                seekBar.max = 0
                tvCurrentTime.text = "00:00"
                tvTotalTime.text = "00:00"
                handler.removeCallbacks(updateProgressTask)
            }
        }

        // 修复：观察home播放的全局播放状态，确保图标绝对一致
        bottomVm.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
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
            // 统一调用全局切换逻辑
            bottomVm.togglePlayPause()
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
            if (name.isNotEmpty() && name != "歌手" && name != "---") {
                val intent = Intent(requireContext(), SingerActivity::class.java)
                intent.putExtra("SINGER_NAME", name)
                startActivity(intent)
            }
        }
    }

    private fun loadSongDetail(songId: Int, img: ImageView, tvTotal: TextView, sb: SeekBar) {
        lifecycleScope.launch {
            val result = songRepository.fetchSongDetail(songId)
            result.onSuccess { detail ->
                val coverUrl = MusicPlayerManager.resolveUrl(detail.cover_url) ?: detail.cover_url
                Glide.with(this@PlayerFragment).load(coverUrl).placeholder(R.drawable.disk).into(img)

                val playUrl = MusicPlayerManager.resolveUrl(detail.play_url)
                if (playUrl == null) {
                    Toast.makeText(requireContext(), "播放地址无效", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }

                MusicPlayerManager.onPrepared = { duration ->
                    sb.max = duration
                    tvTotal.text = formatTime(duration)
                    handler.post(updateProgressTask)
                }

                MusicPlayerManager.onError = { what, extra ->
                    val msg = "无法播放 (Error $what, $extra)"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }

                val isNewPlay = MusicPlayerManager.play(playUrl, detail.song_id)
                
                // 播放启动后，同步一次全局状态让图标变 pause
                bottomVm.syncPlayState()

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
        // 回到页面，同步一次播放器真实状态
        bottomVm.syncPlayState()
        updateProgressBar()
        if (MusicPlayerManager.isPlaying()) {
            handler.post(updateProgressTask)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressTask)
    }
}
