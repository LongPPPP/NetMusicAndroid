package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.netmusicandroid.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.adapter.SearchSongAdapter
import com.example.netmusicandroid.adapter.SearchSingerAdapter
import com.example.netmusicandroid.adapter.UserPlaylistAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.ActivitySearchBinding
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchVm: SearchViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepo = SongRepository.getInstance()

    // 适配器（song / singer / playlist 三套）
    private lateinit var songAdapter: SearchSongAdapter
    private lateinit var singerAdapter: SearchSingerAdapter
    private lateinit var playlistAdapter: UserPlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initAdapters()
        initCategoryButtons()
        initSearchAction()
        observeViewModel()
        initBottomPlayer()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initViewModel() {
        searchVm = ViewModelProvider(this)[SearchViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
    }

    // ── 底部播放栏 ──────────────────────────────

    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
    }

    // ── 分类按钮 ────────────────────────────────

    private fun initCategoryButtons() {
        updateCategoryUI(0) // 默认选中"歌曲"

        binding.tvCatSong.setOnClickListener {
            searchVm.selectCategory(0)
            updateCategoryUI(0)
            showCurrentResults()
        }
        binding.tvCatSinger.setOnClickListener {
            searchVm.selectCategory(1)
            updateCategoryUI(1)
            showCurrentResults()
        }
        binding.tvCatPlaylist.setOnClickListener {
            searchVm.selectCategory(2)
            updateCategoryUI(2)
            showCurrentResults()
        }

        // 返回
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun updateCategoryUI(selected: Int) {
        val normalBg = getDrawable(R.drawable.bg_round_white)
        val selectedBg = getDrawable(R.drawable.bg_category_selected)
        val normalColor = 0xFF212121.toInt()
        val selectedColor = 0xFFFFFFFF.toInt()

        fun apply(v: View, isSel: Boolean) {
            v.background = if (isSel) selectedBg else normalBg
            (v as? android.widget.TextView)?.setTextColor(if (isSel) selectedColor else normalColor)
        }

        apply(binding.tvCatSong, selected == 0)
        apply(binding.tvCatSinger, selected == 1)
        apply(binding.tvCatPlaylist, selected == 2)
    }

    // ── Adapter 初始化 ──────────────────────────

    private fun initAdapters() {
        // 歌曲 Adapter：点击 → 获取完整详情并播放
        songAdapter = SearchSongAdapter { item ->
            lifecycleScope.launch {
                val result = songRepo.fetchSongDetail(item.song_id)
                result.onSuccess { detail ->
                    bottomVm.playSong(detail)
                }.onFailure {
                    Toast.makeText(this@SearchActivity, "获取歌曲详情失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 歌手 Adapter：点击 → 跳转 SingerActivity
        singerAdapter = SearchSingerAdapter { singer ->
            val intent = Intent(this, SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            }
            startActivity(intent)
        }

        // 歌单 Adapter：复用 UserPlaylistAdapter
        // 点击 → 跳转 PlaylistDetailActivity；搜索结果无删除 → onDeleteClick 为空
        playlistAdapter = UserPlaylistAdapter(
            onItemClick = { playlistId ->
                val intent = Intent(this, PlaylistDetailActivity::class.java).apply {
                    putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlistId)
                }
                startActivity(intent)
            },
            onDeleteClick = {} // 搜索结果的歌单不提供删除
        )

        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        // 默认显示歌曲 adapter（使用 swapAdapter 避免 attachToRoot 冲突）
        binding.rvSearchResults.swapAdapter(songAdapter, false)
    }

    // ── 搜索触发 ────────────────────────────────

    private fun initSearchAction() {
        binding.ivSearchIcon.setOnClickListener {
            val keyword = binding.etSearchInput.text?.toString() ?: ""
            searchVm.search(keyword)
        }
    }

    // ── 观察 ViewModel ──────────────────────────

    private fun observeViewModel() {
        // 歌曲结果 → 更新歌曲 Adapter，若当前选中歌曲分类则刷新可见性
        searchVm.songResults.observe(this) { list ->
            songAdapter.submitList(list)
            if (searchVm.selectedCategory.value == 0) {
                applyResultsVisibility(list.isEmpty())
            }
        }

        // 歌手结果
        searchVm.singerResults.observe(this) { list ->
            singerAdapter.submitList(list)
            if (searchVm.selectedCategory.value == 1) {
                applyResultsVisibility(list.isEmpty())
            }
        }

        // 歌单结果
        searchVm.playlistResults.observe(this) { list ->
            playlistAdapter.submitList(list)
            if (searchVm.selectedCategory.value == 2) {
                applyResultsVisibility(list.isEmpty())
            }
        }

        // Toast
        searchVm.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                searchVm.clearToast()
            }
        }

        // Loading
        searchVm.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun applyResultsVisibility(isEmpty: Boolean) {
        binding.rvSearchResults.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    // ── 切换分类 ────────────────────────────────

    /**
     * 切换分类时挂载对应 Adapter。
     *
     * 必须先清除 RecycledViewPool：swapAdapter 回收旧 ViewHolder 到池中，
     * 而三个 Adapter 共用 viewType=0，导致池中 SingerVH 被误复用为 SongVH
     * → ClassCastException 崩溃。清除池后再挂接新 Adapter 可强制 onCreateViewHolder 重建。
     */
    private fun showCurrentResults() {
        // 1. 卸下旧 Adapter，回收当前 ViewHolder 到池中
        binding.rvSearchResults.swapAdapter(null, true)
        // 2. 清空回收池，防止 viewType=0 的旧 ViewHolder 被新 Adapter 复用
        binding.rvSearchResults.recycledViewPool.clear()
        // 3. 挂接新 Adapter（池已空，全部 onCreateViewHolder 重建）
        val (adapter, isEmpty) = when (searchVm.selectedCategory.value) {
            0 -> songAdapter to songAdapter.currentList.isEmpty()
            1 -> singerAdapter to singerAdapter.currentList.isEmpty()
            2 -> playlistAdapter to playlistAdapter.currentList.isEmpty()
            else -> return
        }
        binding.rvSearchResults.swapAdapter(adapter, false)
        binding.rvSearchResults.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
