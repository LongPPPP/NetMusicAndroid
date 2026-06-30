package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.activity.CurrentPlaylistActivity
import com.example.netmusicandroid.activity.FavoritesActivity
import com.example.netmusicandroid.activity.MyCommentActivity
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.activity.RecentPlayActivity
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.activity.SettingActivity
import com.example.netmusicandroid.activity.SingerListActivity
import com.example.netmusicandroid.activity.UploadSongActivity
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.MineViewModel
import com.example.netmusicandroid.viewmodel.MainViewModel
import com.example.netmusicandroid.data.model.SongDetail
import com.example.netmusicandroid.data.repository.PlayQueueRepository
import kotlinx.coroutines.launch

class MineFragment : Fragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    private val mineViewModel: MineViewModel by activityViewModels()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var bottomVm: BottomPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserObserver()
        setupClickListener()
        initBottomPlayer()
    }

    override fun onResume() {
        super.onResume()
        bottomVm.syncPlayState()
    }

    private fun initBottomPlayer() {
        bottomVm = ViewModelProvider(requireActivity())[BottomPlayerViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val bp = LayoutBottomPlayerBinding.bind(binding.root.findViewById(R.id.include_bottom_player))
        bottomVm.songName.observe(viewLifecycleOwner) { bp.tvSongName.text = it }
        bottomVm.singerName.observe(viewLifecycleOwner) { bp.tvSinger.text = it }
        bottomVm.coverUrl.observe(viewLifecycleOwner) { url ->
            ImageLoadUtil.loadImage(bp.ivSongCover, MusicPlayerManager.resolveUrl(url))
        }
        bottomVm.hasCurrentSong.observe(viewLifecycleOwner) { has ->
            bp.root.visibility = if (has) View.VISIBLE else View.GONE
        }
        bottomVm.isPlaying.observe(viewLifecycleOwner) { playing ->
            bp.ivPlayToggle.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
            )
            if (mainViewModel.isPlaying.value != playing) {
                mainViewModel.togglePlayState()
            }
        }
        bottomVm.toastMsg.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                bottomVm.clearToast()
            }
        }
        bp.ivPrev.setOnClickListener {
            bottomVm.playPrev()
            syncCurrentSongToMain()
        }
        bp.ivPlayToggle.setOnClickListener {
            bottomVm.togglePlayPause()
            mainViewModel.togglePlayState()
        }
        bp.ivNext.setOnClickListener {
            bottomVm.playNext()
            syncCurrentSongToMain()
        }

        val goPlayer = View.OnClickListener {
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        bp.cvCover.setOnClickListener(goPlayer)
        bp.llSongInfo.setOnClickListener(goPlayer)
    }

    private fun syncCurrentSongToMain() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repo = PlayQueueRepository()
            val current = repo.getCurrentSong()
            if (current != null) {
                mainViewModel.playSong(
                    SongDetail(
                        song_id = current.song_id,
                        song_name = current.song_name,
                        singer_name = current.singer_name,
                        play_url = current.play_url,
                        cover_url = current.cover_url,
                        duration = current.duration
                    )
                )
            }
        }
    }

    /**
     * 监听Room实时用户数据流，登录/登出/刷新Token自动刷新UI
     */
    private fun setupUserObserver() {
        // 使用 repeatOnLifecycle 安全收集Flow，页面后台自动取消协程
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mineViewModel.currentUserFlow.collect { userEntity: UserEntity? ->
                    renderUserUi(userEntity)
                }
            }
        }
    }

    /**
     * 统一渲染UI，区分【已登录】/【未登录】两种状态
     */
    private fun renderUserUi(user: UserEntity?) {
        if (user == null) {
            // ========== 未登录状态：重置所有信息 ==========
            binding.ivAvatar.setImageResource(R.drawable.avatar_sketch)
            binding.tvUsername.text = "点击头像登录"
            binding.tvSignature.text = ""
            // 隐藏歌手专属菜单
            binding.llPublishSong.visibility = View.GONE
            binding.dividerPublishSong.visibility = View.GONE
            return
        }

        // ========== 已登录状态：填充用户数据 ==========
        Glide.with(this)
            .load(user.avatar)
            .placeholder(R.drawable.avatar_sketch)
            .circleCrop()
            .into(binding.ivAvatar)

        binding.tvUsername.text = user.username
        binding.tvSignature.text = if (user.signature.isBlank()) "这个人很懒，什么都没写" else user.signature
        binding.tvCollectCount.text = user.favoriteCount.toString()
        binding.tvCommentCount.text = user.commentCount.toString()

        // 判断歌手角色
        if (user.role == "ARTIST") {
            binding.llPublishSong.visibility = View.VISIBLE
            binding.dividerPublishSong.visibility = View.VISIBLE
            binding.llUploadSong.visibility = View.VISIBLE
        } else {
            binding.llPublishSong.visibility = View.GONE
            binding.dividerPublishSong.visibility = View.GONE
            binding.llUploadSong.visibility = View.GONE
        }
    }

    /**
     * 点击事件不变，保留原有逻辑
     */
    private fun setupClickListener() {
        // 头像点击跳转登录（未登录时）
        binding.ivAvatar.setOnClickListener {
            // TODO: 跳转登录页面
        }
        binding.llCurrentPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), CurrentPlaylistActivity::class.java))
        }
        binding.llMyFavorite.setOnClickListener {
            startActivity(Intent(requireContext(), FavoritesActivity::class.java))
        }
        binding.llMyComment.setOnClickListener {
            startActivity(Intent(requireContext(), MyCommentActivity::class.java))
        }
        binding.llMyPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }
        binding.llRecentPlay.setOnClickListener {
            startActivity(Intent(requireContext(), RecentPlayActivity::class.java))
        }
        // 点击跳转设置页面 SettingActivity
        binding.llSetting.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }
        binding.llFeedback.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        binding.llPublishSong.setOnClickListener {
            startActivity(Intent(requireContext(), SingerListActivity::class.java))
        }
        binding.llUploadSong.setOnClickListener {
            startActivity(Intent(requireContext(), UploadSongActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}