package com.example.netmusicandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val songRepository = SongRepository()
    private lateinit var songAdapter: HomeSongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val rvHomeSongs = view.findViewById<RecyclerView>(R.id.rvHomeSongs)
        songAdapter = HomeSongAdapter(emptyList()) { song ->
            mainViewModel.playSong(song)
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        rvHomeSongs.adapter = songAdapter

        loadSongs()
    }

    private fun loadSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                // 只显示前 3 首，简单直接
                val displayList = if (list.size > 3) list.take(3) else list
                songAdapter.updateData(displayList)
            }.onFailure { ex ->
                Toast.makeText(requireContext(), "获取歌曲失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
