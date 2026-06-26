package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.netmusicandroid.adapter.UserCollectionAdapter
import com.example.netmusicandroid.databinding.ActivityMyCollectionBinding
import com.example.netmusicandroid.viewmodel.MyCollectionViewModel
import com.example.netmusicandroid.utils.ToastUtil

class MyCollectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyCollectionBinding
    private lateinit var collectionVm: MyCollectionViewModel
    private lateinit var collectionAdapter: UserCollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initAdapter()
        initObserver()
        initClick()
        // 页面加载自动请求全部歌单
        collectionVm.loadUserCollection()
    }

    private fun initViewModel() {
        collectionVm = ViewModelProvider(this)[MyCollectionViewModel::class.java]
    }

    private fun initAdapter() {
        // 删除按钮回调：拿到歌单ID执行DELETE接口
        collectionAdapter = UserCollectionAdapter { targetCollectionId ->
            collectionVm.deleteCollection(targetCollectionId)
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
        // 新建歌单（预留弹窗/跳转逻辑）
        binding.llCreateCollection.setOnClickListener {
            // TODO 弹出新建歌单弹窗
        }
    }
}