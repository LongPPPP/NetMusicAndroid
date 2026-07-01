package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.PlaylistDetailData
import com.example.netmusicandroid.data.repository.PlaylistDetailRepository
import kotlinx.coroutines.launch

class PlaylistDetailViewModel : ViewModel() {
    // 仓库实例
    private val playlistRepo = PlaylistDetailRepository.getInstance()

    // region 数据状态
    // 内部可变，外部只读
    private val _playlistDetail = MutableLiveData<PlaylistDetailData>()
    val playlistDetail: LiveData<PlaylistDetailData> = _playlistDetail

    // Toast提示消息
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    // 加载状态（可选，页面显示loading）
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    // endregion

    /**
     * 加载歌单详情
     */
    fun loadPlaylistDetail(playlistId: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = playlistRepo.getPlaylistDetail(playlistId)
            result.onSuccess { detailData ->
                _playlistDetail.postValue(detailData)
            }.onFailure { error ->
                // 提取异常信息
                val errMsg = error.message ?: "加载歌单失败"
                _toastMsg.postValue(errMsg)
            }
            _isLoading.postValue(false)
        }
    }

    // 删除歌单内指定歌曲
    fun deleteSongInPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = playlistRepo.deleteSongFromPlaylist(playlistId, songId)
            result.onSuccess {
                // 删除成功重新加载歌单刷新列表
                loadPlaylistDetail(playlistId)
            }.onFailure { err ->
                _toastMsg.postValue(err.message ?: "删除歌曲失败")
            }
            _isLoading.postValue(false)
        }
    }

    // 播放全部歌曲（预留）
    fun playAllSong() {
        _toastMsg.postValue("开始播放全部歌曲")
    }

    // 批量操作入口（预留）
    fun openBatchOperate() {
        _toastMsg.postValue("批量操作功能开发中")
    }

    // 重置Toast（页面销毁/重复点击时清空提示，防止重复弹窗）
    fun clearToast() {
        _toastMsg.postValue("")
    }

}