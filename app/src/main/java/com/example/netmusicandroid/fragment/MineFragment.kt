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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.activity.CurrentPlaylistActivity
import com.example.netmusicandroid.activity.FavoritesActivity
import com.example.netmusicandroid.activity.MyCommentActivity
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.activity.RecentPlayActivity
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.activity.SettingActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.activity.UploadSongActivity
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import kotlinx.coroutines.launch

/**
 * 我的页面Fragment
 * 展示用户信息、各类歌单入口、底部全局播放控制器
 */
class MineFragment : Fragment() {
    // ViewBinding 空安全写法，销毁置空防止内存泄漏
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    // 全局共享播放器VM（Activity作用域，全应用唯一实例）
    private val bottomVm: BottomPlayerViewModel by activityViewModels()

    // 创建Fragment布局
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 布局加载完成后初始化逻辑
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserObserver()    // 监听本地用户信息
        setupClickListener()   // 全部条目点击跳转
        initBottomPlayer()     // 初始化底部播放器控件
    }

    // 页面可见时同步播放状态
    override fun onResume() {
        super.onResume()
        bottomVm.syncPlayState()
    }

    /**
     * 初始化底部全局播放栏
     * 绑定ViewModel数据、按钮点击跳转、切歌逻辑
     */
    private fun initBottomPlayer() {
        val bp = LayoutBottomPlayerBinding.bind(binding.root.findViewById(R.id.include_bottom_player))
        BottomPlayerBinder.bind(
            viewLifecycleOwner,
            requireContext(),
            bp,
            bottomVm,
            onOpenPlayer = { (requireActivity() as BaseActivity).navigateToPlayer() }
        )
    }

    /**
     * 监听本地数据库用户Flow，自动刷新用户UI
     * repeatOnLifecycle：页面不可见自动停止收集，避免内存泄漏
     */
    private fun setupUserObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bottomVm.currentUserFlow.collect { userEntity: UserEntity? ->
                    renderUserUi(userEntity)
                }
            }
        }
    }

    /**
     * 根据用户实体渲染页面UI
     * 区分未登录、普通用户、歌手三种显示逻辑
     */
    private fun renderUserUi(user: UserEntity?) {
        // 用户为空：未登录状态
        if (user == null) {
            binding.ivAvatar.setImageResource(R.drawable.avatar_sketch)
            binding.tvUsername.text = "点击头像登录"
            binding.tvSignature.text = ""
            // 隐藏歌手上传相关入口
            binding.llPublishSong.visibility = View.GONE
            binding.dividerPublishSong.visibility = View.GONE
            return
        }

        // 已登录：统一url解析，和播放器封面处理规则完全相同
        val rawAvatarPath = user.avatar
        val avatarUrl = if (rawAvatarPath.isNullOrBlank()) null else MusicPlayerManager.resolveUrl(rawAvatarPath) ?: rawAvatarPath

        // 跳过磁盘缓存确保头像修改后立即刷新（头像URL可能不变但服务端文件已替换）
        Glide.with(this@MineFragment)
            .load(avatarUrl)
            .placeholder(R.drawable.avatar_sketch)
            .error(R.drawable.avatar_sketch)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .circleCrop()
            .into(binding.ivAvatar)

        binding.tvUsername.text = user.username
        binding.tvSignature.text = if (user.signature.isBlank()) "这个人很懒，什么都没写" else user.signature

        // 判断角色：歌手显示发布/上传歌曲入口，普通用户隐藏
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
     * 页面所有条目点击事件，跳转对应Activity
     */
    private fun setupClickListener() {
        // 头像点击登录（待实现）
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
        binding.llSetting.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }
        binding.llFeedback.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        // 歌手：我的作品 → 先查歌手ID，再跳转歌手详情页
        binding.llPublishSong.setOnClickListener {
            lifecycleScope.launch {
                val result = AuthRepository.getInstance().getMySingerId()
                result.onSuccess { singerId ->
                    val intent = Intent(requireContext(), SingerActivity::class.java)
                    intent.putExtra("SINGER_ID", singerId)
                    startActivity(intent)
                }.onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "获取歌手信息失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 歌手：上传歌曲
        binding.llUploadSong.setOnClickListener {
            startActivity(Intent(requireContext(), UploadSongActivity::class.java))
        }
    }

    // Fragment销毁清空binding，防止内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}