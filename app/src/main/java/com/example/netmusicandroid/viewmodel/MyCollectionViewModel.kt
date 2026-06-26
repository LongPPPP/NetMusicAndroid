package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.api.ApiResponse
import com.example.netmusicandroid.bean.CollectionData
import com.example.netmusicandroid.bean.UserCollectionBean
import com.example.netmusicandroid.repository.MineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MyCollectionViewModel : ViewModel() {
    private val mineRepo = MineRepository()

    // 歌单列表数据源
    private val _collectionList = MutableLiveData<List<UserCollectionBean>>()
    val collectionList: LiveData<List<UserCollectionBean>> = _collectionList

    // 弹窗提示文字
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    // 加载全部用户歌单（协程写法，无enqueue回调）
    fun loadUserCollection() {
        viewModelScope.launch {
            try {
                val userId = 1 // 替换成你登录接口拿到的真实用户id
                val resp = mineRepo.getUserCollection(userId)
                if (resp.isSuccessful) {
                    val list = resp.body()?.data?.list ?: emptyList()
                    _collectionList.postValue(list)
                } else {
                    _toastMsg.postValue("加载歌单失败")
                }
            } catch (e: Exception) {
                _toastMsg.postValue("网络异常：${e.message}")
            }
        }
    }

    // 删除歌单接口 DELETE /api/v1/playlists/{collectionId}
    fun deleteCollection(collectionId: Int) {
        viewModelScope.launch {
            try {
                val resp: Response<ApiResponse<Any>> = mineRepo.deleteCollection(collectionId)
                if (resp.isSuccessful && resp.body()?.code == 200) {
                    _toastMsg.postValue("删除成功")
                    // 删除后刷新列表
                    loadUserCollection()
                } else {
                    _toastMsg.postValue("删除失败，请重试")
                }
            } catch (t: Throwable) {
                _toastMsg.postValue("删除请求失败：${t.message}")
            }
        }
    }


}