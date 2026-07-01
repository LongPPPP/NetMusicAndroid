package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.SearchSingerAdapter
import com.example.netmusicandroid.adapter.SearchSongAdapter
import com.example.netmusicandroid.adapter.UserPlaylistAdapter
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.databinding.ActivitySearchBinding
import com.example.netmusicandroid.ui.UiEvent
import com.example.netmusicandroid.ui.base.BaseBindingActivity
import com.example.netmusicandroid.ui.state.SearchUiState
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

class SearchActivity : BaseBindingActivity<ActivitySearchBinding>() {

    private lateinit var searchVm: SearchViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private val songRepo = SongRepository.getInstance()

    private lateinit var songAdapter: SearchSongAdapter
    private lateinit var singerAdapter: SearchSingerAdapter
    private lateinit var playlistAdapter: UserPlaylistAdapter
    private var attachedCategory = -1

    override fun inflateBinding(inflater: LayoutInflater): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun initBottomPlayer() {
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
    }

    private fun initCategoryButtons() {
        binding.tvCatSong.setOnClickListener { searchVm.selectCategory(0) }
        binding.tvCatSinger.setOnClickListener { searchVm.selectCategory(1) }
        binding.tvCatPlaylist.setOnClickListener { searchVm.selectCategory(2) }
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

    private fun initAdapters() {
        songAdapter = SearchSongAdapter { item ->
            lifecycleScope.launch {
                songRepo.fetchSongDetail(item.song_id)
                    .onSuccess { detail -> bottomVm.playSong(detail) }
                    .onFailure { ToastUtil.showShort("获取歌曲详情失败") }
            }
        }

        singerAdapter = SearchSingerAdapter { singer ->
            startActivity(Intent(this, SingerActivity::class.java).apply {
                putExtra("SINGER_NAME", singer.singer_name)
            })
        }

        playlistAdapter = UserPlaylistAdapter(
            onItemClick = { playlistId ->
                startActivity(Intent(this, PlaylistDetailActivity::class.java).apply {
                    putExtra(PlaylistActivity.EXTRA_PLAYLIST_ID, playlistId)
                })
            },
            onDeleteClick = {}
        )

        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
    }

    private fun initSearchAction() {
        binding.ivSearchIcon.setOnClickListener {
            val keyword = binding.etSearchInput.text?.toString() ?: ""
            searchVm.search(keyword)
        }
    }

    private fun observeViewModel() {
        searchVm.uiState.observe(this) { render(it) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchVm.events.collect { event ->
                    when (event) {
                        is UiEvent.Toast -> ToastUtil.showShort(event.message)
                    }
                }
            }
        }
    }

    private fun render(state: SearchUiState) {
        updateCategoryUI(state.selectedCategory)
        songAdapter.submitList(state.songResults)
        singerAdapter.submitList(state.singerResults)
        playlistAdapter.submitList(state.playlistResults)
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        showCurrentResults(state)
    }

    /**
     * 切换分类时挂载对应 Adapter。
     *
     * 必须先清除 RecycledViewPool：swapAdapter 回收旧 ViewHolder 到池中，
     * 而三个 Adapter 共用 viewType=0，导致池中 SingerVH 被误复用为 SongVH
     * → ClassCastException 崩溃。清除池后再挂接新 Adapter 可强制 onCreateViewHolder 重建。
     */
    private fun showCurrentResults(state: SearchUiState) {
        if (attachedCategory != state.selectedCategory) {
            binding.rvSearchResults.swapAdapter(null, true)
            binding.rvSearchResults.recycledViewPool.clear()
            binding.rvSearchResults.swapAdapter(adapterFor(state.selectedCategory), false)
            attachedCategory = state.selectedCategory
        }
        binding.rvSearchResults.visibility = if (currentListIsEmpty(state)) View.GONE else View.VISIBLE
    }

    private fun adapterFor(category: Int): RecyclerView.Adapter<*> = when (category) {
        0 -> songAdapter
        1 -> singerAdapter
        2 -> playlistAdapter
        else -> songAdapter
    }

    private fun currentListIsEmpty(state: SearchUiState): Boolean = when (state.selectedCategory) {
        0 -> state.songResults.isEmpty()
        1 -> state.singerResults.isEmpty()
        2 -> state.playlistResults.isEmpty()
        else -> true
    }
}
