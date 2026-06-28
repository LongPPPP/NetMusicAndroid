package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.viewmodel.MineViewModel
import kotlinx.coroutines.launch

class MineFragment : Fragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    private val mineViewModel: MineViewModel by activityViewModels()

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
        // 删除：mineViewModel.loadLocalUserInfo(requireContext())
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

        // 判断歌手角色
        if (user.role == "ARTIST") {
            binding.llPublishSong.visibility = View.VISIBLE
            binding.dividerPublishSong.visibility = View.VISIBLE
        } else {
            binding.llPublishSong.visibility = View.GONE
            binding.dividerPublishSong.visibility = View.GONE
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
        binding.llMyPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }
        binding.llRecentPlay.setOnClickListener {}
        binding.llSetting.setOnClickListener {}
        binding.llFeedback.setOnClickListener {}
        binding.llPublishSong.setOnClickListener {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}