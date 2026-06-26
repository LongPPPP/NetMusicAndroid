package com.example.netmusicandroid.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.MyCollectionActivity
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.viewmodel.MineViewModel

class MineFragment : Fragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private lateinit var mineViewModel: MineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initObserver()
        initClickEvent()
        // 页面加载请求用户信息接口
        mineViewModel.loadUserInfo()
    }

    // 初始化ViewModel：使用Activity作用域，多Fragment共享数据
    private fun initViewModel() {
        mineViewModel = ViewModelProvider(requireActivity())[MineViewModel::class.java]
    }

    // 监听用户数据渲染UI（适配后端返回字段，移除nickname、level等不存在字段）
    private fun initObserver() {
        mineViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            // 头像：avatar为null时展示默认占位图
            Glide.with(this)
                .load(user.avatar)
                .circleCrop()
                .placeholder(R.drawable.avatar_sketch)
                .into(binding.ivAvatar)

            // 用户名（后端必返回，非空）
            binding.tvUsername.text = user.username
            // 个性签名，空值兜底展示默认文案
            binding.tvSignature.text = user.signature ?: "这个人很懒，什么都没写"

            // binding.tvCollectCount.text = user.collectCount.toString()
            // binding.tvCommentCount.text = user.commentCount.toString()
        }
    }

    // 全部点击事件
    private fun initClickEvent() {
        // 我的歌单 跳转二级页面MyCollectionActivity歌单列表
        binding.llMyPlaylist.setOnClickListener {
            val intent = Intent(requireContext(), MyCollectionActivity::class.java)
            startActivity(intent)
        }

        // 最近播放（预留跳转）
        binding.llRecentPlay.setOnClickListener {
            // TODO 跳转最近播放页面
        }

        // 设置
        binding.llSetting.setOnClickListener {
            // TODO 跳转设置页面
        }

        // 帮助反馈
        binding.llFeedback.setOnClickListener {
            // TODO 跳转反馈页面
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放binding，防止内存泄漏
        _binding = null
    }
}