package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SingerDetail
import com.example.netmusicandroid.data.repository.SingerRepository
import kotlinx.coroutines.launch

class SingerViewModel : ViewModel() {

    private val repository = SingerRepository()

    // 歌手详情数据
    private val _singerDetail = MutableLiveData<SingerDetail?>()
    val singerDetail: LiveData<SingerDetail?> = _singerDetail

    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * 根据歌手名字加载详情
     * 逻辑：列表找ID -> 请求详情
     */
    fun loadSingerByName(name: String) {
        viewModelScope.launch {
            // 1. 获取歌手列表
            val listResult = repository.fetchSingers(1)
            listResult.onSuccess { list ->
                // 2. 匹配名字找 ID
                val singer = list.find { it.singer_name == name }
                if (singer != null) {
                    fetchDetail(singer.singer_id)
                } else {
                    _error.value = "未找到歌手信息"
                }
            }.onFailure {
                _error.value = "加载歌手列表失败"
            }
        }
    }

    private fun fetchDetail(id: Int) {
        viewModelScope.launch {
            val result = repository.fetchSingerDetail(id)
            result.onSuccess {
                _singerDetail.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }
}
