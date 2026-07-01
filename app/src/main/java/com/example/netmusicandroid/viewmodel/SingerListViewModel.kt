package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.SingerItem
import com.example.netmusicandroid.data.repository.SingerRepository
import kotlinx.coroutines.launch

class SingerListViewModel : ViewModel() {

    private val repo = SingerRepository()

    private val _singers = MutableLiveData<List<SingerItem>>(emptyList())
    val singers: LiveData<List<SingerItem>> = _singers

    private val _toastMsg = MutableLiveData("")
    val toastMsg: LiveData<String> = _toastMsg

    fun loadSingers() {
        viewModelScope.launch {
            repo.fetchSingers().onSuccess { list ->
                _singers.postValue(list)
            }.onFailure { e ->
                _toastMsg.postValue(e.message ?: "加载歌手列表失败")
            }
        }
    }

    fun clearToast() {
        _toastMsg.postValue("")
    }
}
