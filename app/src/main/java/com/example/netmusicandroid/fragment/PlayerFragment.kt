package com.example.netmusicandroid.fragment

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
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
import com.example.netmusicandroid.data.repository.PlaylistRepository
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.dialog.AddToPlaylistDialog
import com.example.netmusicandroid.sp.SpManager
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import kotlinx.coroutines.launch


/**
 * 全屏播放器Fragment
 * 播放状态、切歌逻辑完全与底部迷你播放栏同源，统一由BottomPlayerViewModel管理
 * 修复：进度条切歌乱跳问题，严格控制刷新时机与任务生命周期
 */
class PlayerFragment : Fragment() {
    // 全局共享播放器VM，与底部迷你播放栏共用同一实例
    private lateinit var bottomVm: BottomPlayerViewModel
    // 歌曲数据仓库，仅用于补全歌曲详情UI数据
    private val songRepository = SongRepository.getInstance()
    // 歌单仓库，用于加载歌单列表和添加歌曲到歌单
    private val playlistRepository = PlaylistRepository.getInstance()

    // 当前播放歌曲ID，避免重复加载详情
    private var currentSongId: Int = -1

    // 主线程Handler，定时刷新播放进度
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressTask = object : Runnable {
        override fun run() {
            // 修复：播放器未准备完成时不刷新进度，避免异常数值
            if (MusicPlayerManager.getDuration() > 0) {
                updateProgressBar()
            }
            handler.postDelayed(this, 1000)
        }
    }

    // 播放器准备完成监听器，用于精准更新时长、启动进度
    private val preparedListener: (Int) -> Unit = { duration ->
        view?.let { root ->
            val seekBar = root.findViewById<SeekBar>(R.id.seekBar)
            val tvTotalTime = root.findViewById<TextView>(R.id.tvTotalTime)
            seekBar.max = duration
            tvTotalTime.text = formatTime(duration)
            // 准备完成且正在播放时，启动进度刷新
            if (bottomVm.isPlaying.value == true) {
                handler.post(updateProgressTask)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取Activity级共享VM，与底部播放栏完全共用状态
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]
        currentSongId = MusicPlayerManager.getCurrentSongId()

        // 绑定所有控件
        val tvSongName = view.findViewById<TextView>(R.id.tvSongName)
        val tvSinger = view.findViewById<TextView>(R.id.tvSinger)
        val imgNeedle = view.findViewById<ImageView>(R.id.imgNeedle)
        val recordContainer = view.findViewById<View>(R.id.recordContainer)
        val imgCover = view.findViewById<ImageView>(R.id.imgCover)
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNext)
        val btnLike = view.findViewById<ImageButton>(R.id.btnLike)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvCurrentTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvTotalTime = view.findViewById<TextView>(R.id.tvTotalTime)
        val recordRotateAnimator = ObjectAnimator.ofFloat(recordContainer, View.ROTATION, 0f, 360f).apply {
            duration = 20000L
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        // 页面初始化同步已有进度
        val currentDuration = MusicPlayerManager.getDuration()
        if (currentDuration > 0) {
            seekBar.max = currentDuration
            seekBar.progress = MusicPlayerManager.getCurrentPosition()
            tvTotalTime.text = formatTime(currentDuration)
            tvCurrentTime.text = formatTime(MusicPlayerManager.getCurrentPosition())
        }

        // 注册播放器准备完成监听，精准更新时长
        MusicPlayerManager.addOnPreparedListener(preparedListener)

        // 监听全局歌曲切换，统一刷新UI（与迷你播放栏同源）
        bottomVm.currentSong.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                val isNewSong = currentSongId != song.song_id
                currentSongId = song.song_id

                // 基础信息直接更新
                tvSongName.text = song.song_name
                tvSinger.text = song.singer_name

                if (isNewSong) {
                    // 修复：切新歌先立刻停止所有旧进度任务，清空残留回调，避免旧进度跳变
                    handler.removeCallbacks(updateProgressTask)

                    // 强制重置进度UI，从0开始
                    seekBar.progress = 0
                    tvCurrentTime.text = "00:00"
                    // 总时长先临时清空，等待播放器准备完成后更新真实值
                    seekBar.max = 0
                    tvTotalTime.text = "00:00"

                    // 异步加载详情补全封面等UI，不控制播放
                    loadSongDetail(song.song_id, imgCover, tvTotalTime, seekBar)
                } else {
                    // 同一首歌：直接同步封面与进度
                    val coverUrl = MusicPlayerManager.resolveUrl(song.cover_url) ?: song.cover_url
                    Glide.with(this@PlayerFragment)
                        .load(coverUrl)
                        .placeholder(R.drawable.disk)
                        .into(imgCover)

                    val duration = MusicPlayerManager.getDuration()
                    if (duration > 0) {
                        seekBar.max = duration
                        tvTotalTime.text = formatTime(duration)
                        updateProgressBar()
                    }
                }
            } else {
                // 无播放歌曲，清空所有UI并停止进度刷新
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

        // 监听全局播放状态，同步播放/暂停按钮
        bottomVm.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                btnPlay.setImageResource(R.drawable.pause_button)
                if (!recordRotateAnimator.isStarted) {
                    recordRotateAnimator.start()
                } else {
                    recordRotateAnimator.resume()
                }
                imgNeedle.animate().rotation(0f).setDuration(220L).start()
                // 修复：只有播放器已准备完成，才启动进度刷新，避免提前启动乱跳
                if (MusicPlayerManager.getDuration() > 0) {
                    handler.removeCallbacks(updateProgressTask)
                    handler.post(updateProgressTask)
                }
            } else {
                btnPlay.setImageResource(R.drawable.play_button)
                if (recordRotateAnimator.isStarted) {
                    recordRotateAnimator.pause()
                }
                imgNeedle.animate().rotation(-28f).setDuration(220L).start()
                handler.removeCallbacks(updateProgressTask)
            }
        }

        // 播放/暂停：统一走VM控制，全局状态同步
        btnPlay.setOnClickListener {
            if (currentSongId == -1) return@setOnClickListener
            bottomVm.togglePlayPause()
        }

        // 上一首：统一走VM控制，与迷你播放栏逻辑完全一致
        btnPrev.setOnClickListener { bottomVm.playPrev() }

        // 下一首：统一走VM控制，与迷你播放栏逻辑完全一致
        btnNext.setOnClickListener { bottomVm.playNext() }

        // 添加到歌单
        val btnAddPlaylist = view.findViewById<ImageButton>(R.id.btnAddPlaylist)
        btnAddPlaylist.setOnClickListener {
            val songId = currentSongId
            if (songId == -1) {
                Toast.makeText(requireContext(), "暂无播放歌曲", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val userId = SpManager.getUserId().toInt()
                val resp = playlistRepository.getUserPlaylist(userId)
                if (resp.isSuccessful) {
                    val plist = resp.body()?.data?.list ?: emptyList()
                    if (plist.isEmpty()) {
                        Toast.makeText(requireContext(), "暂无歌单，请先创建歌单", Toast.LENGTH_SHORT).show()
                    } else {
                        AddToPlaylistDialog(requireContext(), plist) { selected ->
                            lifecycleScope.launch {
                                val result = playlistRepository.addSongToPlaylist(
                                    selected.playlist_id, songId
                                )
                                result.onSuccess { msg ->
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                }.onFailure { e ->
                                    Toast.makeText(requireContext(), e.message ?: "添加失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.show()
                    }
                } else {
                    Toast.makeText(requireContext(), "加载歌单失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 收藏功能逻辑
        btnLike.setOnClickListener {
            if (currentSongId != -1) {
                bottomVm.toggleFavorite()
            }
        }

        // 监听收藏状态，更新爱心图标
        bottomVm.isLiked.observe(viewLifecycleOwner) { liked ->
            if (liked) {
                btnLike.setImageResource(R.drawable.ic_heart_filled)
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_empty)
            }
        }

        // 进度条拖拽逻辑
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                handler.removeCallbacks(updateProgressTask)
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { MusicPlayerManager.seekTo(it.progress) }
                if (bottomVm.isPlaying.value == true && MusicPlayerManager.getDuration() > 0) {
                    handler.post(updateProgressTask)
                }
            }
        })

        // 评论按钮跳转
        view.findViewById<ImageButton>(R.id.btnComment).setOnClickListener {
            if (currentSongId == -1) return@setOnClickListener
            val intent = Intent(requireContext(), CommentActivity::class.java)
            intent.putExtra("SONG_ID", currentSongId)
            startActivity(intent)
        }

        // 歌手名跳转歌手主页
        tvSinger.setOnClickListener {
            val name = tvSinger.text.toString()
            if (name.isNotEmpty() && name != "歌手" && name != "---") {
                val intent = Intent(requireContext(), SingerActivity::class.java)
                intent.putExtra("SINGER_NAME", name)
                startActivity(intent)
            }
        }

        imgNeedle.post {
            imgNeedle.pivotX = 0f
            imgNeedle.pivotY = 0f

            Log.d("TEST", "${imgNeedle.pivotX},${imgNeedle.pivotY}")
            imgNeedle.rotation = 30f
        }
    }

    /**
     * 仅加载歌曲详情用于UI渲染，不控制播放
     * 播放逻辑统一由BottomPlayerViewModel管理，避免状态冲突
     */
    private fun loadSongDetail(songId: Int, img: ImageView, tvTotal: TextView, sb: SeekBar) {
        lifecycleScope.launch {
            val result = songRepository.fetchSongDetail(songId)
            result.onSuccess { detail ->
                // 加载封面
                val coverUrl = MusicPlayerManager.resolveUrl(detail.cover_url) ?: detail.cover_url
                Glide.with(this@PlayerFragment).load(coverUrl).placeholder(R.drawable.disk).into(img)

                // 仅当播放器还没准备完成时，用详情时长做临时显示
                val realDuration = MusicPlayerManager.getDuration()
                if (realDuration <= 0) {
                    val detailDuration = detail.duration ?: 0
                    if (detailDuration > 0) {
                        sb.max = detailDuration
                        tvTotal.text = formatTime(detailDuration)
                    }
                }
            }.onFailure {
                context?.let {
                    Toast.makeText(it, "加载歌曲详情失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 刷新进度条与当前播放时间 */
    private fun updateProgressBar() {
        val current = MusicPlayerManager.getCurrentPosition()
        view?.let { root ->
            root.findViewById<SeekBar>(R.id.seekBar)?.progress = current
            root.findViewById<TextView>(R.id.tvCurrentTime)?.text = formatTime(current)
        }
    }

    /** 毫秒转 分:秒 格式 */
    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    override fun onResume() {
        super.onResume()
        // 页面可见时同步播放状态与进度
        bottomVm.syncPlayState()
        if (MusicPlayerManager.getDuration() > 0) {
            updateProgressBar()
            if (bottomVm.isPlaying.value == true) {
                handler.removeCallbacks(updateProgressTask)
                handler.post(updateProgressTask)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除所有定时任务与监听器，防止内存泄漏
        handler.removeCallbacks(updateProgressTask)
        MusicPlayerManager.removeOnPreparedListener(preparedListener)
    }
}