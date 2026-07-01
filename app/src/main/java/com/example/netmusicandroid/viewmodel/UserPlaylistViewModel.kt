package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.UserPlaylist
import com.example.netmusicandroid.data.repository.PlaylistRepository
import com.example.netmusicandroid.sp.SpManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class UserPlaylistViewModel : ViewModel() {
    private val mineRepo = PlaylistRepository.getInstance()

    // 歌单列表数据源
    private val _collectionList = MutableLiveData<List<UserPlaylist>>()
    val collectionList: LiveData<List<UserPlaylist>> = _collectionList

    // 弹窗提示文字
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    // 加载全部用户歌单（协程写法，无enqueue回调）
    fun loadUserCollection() {
        viewModelScope.launch {
            try {
                val userId = SpManager.getUserId().toInt()
                val resp = mineRepo.getUserPlaylist(userId)
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

    // 创建歌单
    fun createUserPlaylist(name: String) {
        viewModelScope.launch {
            try {
                val resp = mineRepo.createUserPlaylist(name)

                if (resp != null && resp.code == 201) {
                    _toastMsg.postValue("创建成功")
                    // 创建成功后刷新列表
                    delay(300)
                    loadUserCollection()
                } else {
                    _toastMsg.postValue(resp?.message ?: "创建失败")
                }

            } catch (e: Exception) {
                _toastMsg.postValue("网络异常：${e.message}")
            }
        }
    }


    // 删除歌单接口 DELETE /api/v1/playlists/{collectionId}
    fun deleteUserPlaylist(collectionId: Int) {
        viewModelScope.launch {
            try {
                val resp: Response<ApiResponse<Any>> = mineRepo.deleteUserPlaylist(collectionId)
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