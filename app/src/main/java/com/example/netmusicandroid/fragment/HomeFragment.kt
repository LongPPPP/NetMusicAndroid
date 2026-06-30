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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.activity.SearchActivity
import com.example.netmusicandroid.activity.SingerActivity
import com.example.netmusicandroid.adapter.HomeSingerAdapter
import com.example.netmusicandroid.adapter.HomeSongAdapter
import com.example.netmusicandroid.data.repository.SingerRepository
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.viewmodel.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val songRepository = SongRepository()
    private val singerRepository = SingerRepository()
    
    private lateinit var songAdapter: HomeSongAdapter
    private lateinit var singerAdapter: HomeSingerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        // 1. 初始化歌曲列表
        val rvHomeSongs = view.findViewById<RecyclerView>(R.id.rvHomeSongs)
        songAdapter = HomeSongAdapter(emptyList()) { song ->
            mainViewModel.playSong(song)
            (requireActivity() as BaseActivity).navigateToPlayer()
        }
        rvHomeSongs.adapter = songAdapter
        rvHomeSongs.isNestedScrollingEnabled = false

        // 2. 初始化歌手列表
        val rvHomeSingers = view.findViewById<RecyclerView>(R.id.rvHomeSingers)
        singerAdapter = HomeSingerAdapter(emptyList()) { singer ->
            val intent = Intent(requireContext(), SingerActivity::class.java)
            intent.putExtra("SINGER_NAME", singer.singer_name)
            startActivity(intent)
        }
        rvHomeSingers.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvHomeSingers.adapter = singerAdapter

        // 3. 搜索功能
        val toSearch = Intent(requireContext(), SearchActivity::class.java)
        view.findViewById<View>(R.id.ll_search_bar).setOnClickListener { startActivity(toSearch) }
        view.findViewById<View>(R.id.btnSearch).setOnClickListener { startActivity(toSearch) }

        loadData()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 加载歌手
            launch {
                singerRepository.fetchSingers(1).onSuccess { list ->
                    singerAdapter.updateData(list)
                }
            }

            // 加载歌曲并补全封面
            val result = songRepository.fetchSongs(1)
            result.onSuccess { list ->
                val displayList = (if (list.size > 3) list.take(3) else list).toMutableList()
                songAdapter.updateData(displayList)

                val details = coroutineScope {
                    displayList.map { song ->
                        async { songRepository.fetchSongDetail(song.song_id) }
                    }.awaitAll()
                }
                details.forEachIndexed { index, detailResult ->
                    detailResult.onSuccess { displayList[index] = it }
                }
                songAdapter.updateData(displayList.toList())
            }.onFailure { ex ->
                Toast.makeText(requireContext(), "加载失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
