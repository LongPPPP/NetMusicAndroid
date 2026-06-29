package com.example.netmusicandroid.fragment

import android.content.Intent
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
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

        // 搜索框 + 搜索图标 → 跳转搜索页面
        val toSearch = Intent(requireContext(), SearchActivity::class.java)
        view.findViewById<View>(R.id.ll_search_bar).setOnClickListener { startActivity(toSearch) }
        view.findViewById<View>(R.id.btnSearch).setOnClickListener { startActivity(toSearch) }

        loadSongs()
    }

    private fun loadSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                val displayList = (if (list.size > 3) list.take(3) else list).toMutableList()
                // 先展示基本信息（封面用占位图）
                songAdapter.updateData(displayList)

                // 并行获取所有歌曲详情，全部完成后统一更新一次 Adapter
                val details = coroutineScope {
                    displayList.map { song ->
                        async { songRepository.fetchSongDetail(song.song_id) }
                    }.awaitAll()
                }
                // 用详情结果替换列表项，再次提交（只触发一次 DiffUtil + 绑定）
                details.forEachIndexed { index, detailResult ->
                    detailResult.onSuccess { displayList[index] = it }
                }
                songAdapter.updateData(displayList.toList())
            }.onFailure { ex ->
                Toast.makeText(requireContext(), "获取歌曲失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
