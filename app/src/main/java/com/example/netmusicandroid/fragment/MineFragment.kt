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
import com.example.netmusicandroid.activity.UploadSongActivity
import com.example.netmusicandroid.databinding.FragmentMineBinding
import com.example.netmusicandroid.viewmodel.MineViewModel

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
        setupObserver()
        setupClickListener()
        mineViewModel.loadUserInfo()
    }

    private fun setupObserver() {
        mineViewModel.userInfo.observe(viewLifecycleOwner) { user ->
            Glide.with(this)
                .load(user.avatar)
                .placeholder(R.drawable.avatar_sketch)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvUsername.text = user.username
            binding.tvSignature.text = user.signature ?: "这个人很懒，什么都没写"

            // 如果是歌手，显示上架歌曲菜单
            if (user.role == "ARTIST") {
                binding.llUploadSong.visibility = View.VISIBLE
            } else {
                binding.llUploadSong.visibility = View.GONE
            }
        }
    }

    private fun setupClickListener() {
        binding.llMyPlaylist.setOnClickListener {
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }

        binding.llUploadSong.setOnClickListener {
            startActivity(Intent(requireContext(), UploadSongActivity::class.java))
        }

        binding.llRecentPlay.setOnClickListener { /* TODO */ }
        binding.llSetting.setOnClickListener { /* TODO */ }
        binding.llFeedback.setOnClickListener { /* TODO */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
