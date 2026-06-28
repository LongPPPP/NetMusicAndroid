package com.example.netmusicandroid.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.adapter.UserPlaylistAdapter
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.databinding.ActivityPlaylistBinding
import com.example.netmusicandroid.dialog.CreatePlaylistDialog
import com.example.netmusicandroid.viewmodel.UserPlaylistViewModel
import com.example.netmusicandroid.utils.ToastUtil
import kotlinx.coroutines.launch

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var collectionVm: UserPlaylistViewModel
    private lateinit var collectionAdapter: UserPlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initAdapter()
        initObserver()
        initClick()
        // 页面加载自动请求全部歌单
        collectionVm.loadUserCollection()
    }

    private fun initViewModel() {
        collectionVm = ViewModelProvider(this)[UserPlaylistViewModel::class.java]
    }

    private fun initAdapter() {
        // 删除按钮回调：拿到歌单ID执行DELETE接口
        collectionAdapter = UserPlaylistAdapter { targetCollectionId ->
            collectionVm.deleteUserPlaylist(targetCollectionId)
        }
        // 修复列表空白警告核心代码
        binding.rvCollectionList.layoutManager = LinearLayoutManager(this)
        binding.rvCollectionList.adapter = collectionAdapter
    }

    private fun initObserver() {
        // 监听歌单列表刷新UI
        collectionVm.collectionList.observe(this) { list ->
            collectionAdapter.submitList(list)
            binding.tvTotalCount.text = "共 ${list.size} 个歌单"
        }
        // 弹窗提示（删除成功/失败、网络异常）
        collectionVm.toastMsg.observe(this) { msg ->
            ToastUtil.showShort(msg)
        }
    }

    private fun initClick() {
        // 返回我的页面
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 新建歌单：弹出独立 Dialog，通过回调调用 ViewModel
        binding.llCreateUserPlaylist.setOnClickListener {
            CreatePlaylistDialog(this) { playlistName ->
                collectionVm.createUserPlaylist(playlistName)
            }.show()
        }
    }
}