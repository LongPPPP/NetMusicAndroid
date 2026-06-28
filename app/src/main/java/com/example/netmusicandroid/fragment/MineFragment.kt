package com.example.netmusicandroid.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.PlaylistActivity
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.viewmodel.MineViewModel

class MineFragment : Fragment() {
    // ViewBinding 标准空安全写法
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    // 共享Activity级别的ViewModel
    private val mineViewModel: MineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflate 三个参数标准Fragment写法，对应你改造后的 fragment_mine.xml
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化监听、点击、加载用户数据
        setupObserver()
        setupClickListener()
        mineViewModel.loadUserInfo()
    }

    // 监听ViewModel用户信息LiveData，自动刷新UI
    private fun setupObserver() {
        mineViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            // 头像Glide圆形加载
            Glide.with(this)
                .load(user.avatar)
                .placeholder(R.drawable.avatar_sketch)
                .circleCrop()
                .into(binding.ivAvatar)

            // 赋值用户名、个性签名
            binding.tvUsername.text = user.username
            binding.tvSignature.text = user.signature ?: "这个人很懒，什么都没写"

            // 统计数据（接口完善后取消注释）
            // binding.tvCollectCount.text = user.collectCount.toString()
            // binding.tvCommentCount.text = user.commentCount.toString()
        }
    }

    // 页面全部点击事件
    private fun setupClickListener() {
        // 跳转我的歌单页面
        binding.llMyPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }
        // 最近播放
        binding.llRecentPlay.setOnClickListener {
            // TODO 最近播放页面跳转
        }
        // 设置
        binding.llSetting.setOnClickListener {
            // TODO 设置页面
        }
        // 帮助反馈
        binding.llFeedback.setOnClickListener {
            // TODO 反馈弹窗/页面
        }
    }

    // Fragment销毁置空binding，防止内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}