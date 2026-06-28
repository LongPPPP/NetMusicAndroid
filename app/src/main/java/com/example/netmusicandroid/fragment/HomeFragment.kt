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
            // 1. 先获取列表
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                // 象征性只取前 3 首，并转为可变列表以便后续补全数据
                val displayList = (if (list.size > 3) list.take(3) else list).toMutableList()
                
                // 先显示出名字（此时封面还是占位图）
                songAdapter.updateData(displayList)

                // 2. 【方案 B 核心】：针对显示的这几首歌，挨个去取详情（拿到封面）
                displayList.forEachIndexed { index, song ->
                    launch { // 开启子协程并行获取，速度更快
                        val detailResult = songRepository.fetchSongDetail(song.song_id)
                        detailResult.onSuccess { fullDetail ->
                            // 用含有封面的完整对象替换旧对象
                            displayList[index] = fullDetail
                            // 刷新适配器显示封面
                            songAdapter.updateData(displayList.toList())
                        }
                    }
                }

            }.onFailure { ex ->
                Toast.makeText(requireContext(), "获取歌曲失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
