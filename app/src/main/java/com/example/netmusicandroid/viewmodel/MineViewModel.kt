package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.model.ApiResponse
import com.example.netmusicandroid.data.model.bean.UserBean
import com.example.netmusicandroid.data.repository.MineRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class MineViewModel : ViewModel() {
    private val repo = MineRepository()
    // 私有可变数据源
    private val _userInfo = MutableLiveData<UserBean>()
    // 对外只读暴露
    val userInfo: LiveData<UserBean> = _userInfo

    // 协程版本加载用户信息，移除Call回调
    fun loadUserInfo() {
        // viewModelScope 自动绑定页面生命周期，页面销毁自动取消网络请求
        viewModelScope.launch {
            try {
                // 调用repository的suspend挂起函数
                val response: Response<ApiResponse<UserBean>> = repo.getUserInfo()
                if (response.isSuccessful) {
                    val apiResp = response.body()
                    // 判断业务码200代表接口业务成功
                    if (apiResp?.code == 200) {
                        val userData = apiResp.data
                        userData?.let {
                            // 更新LiveData，通知Fragment刷新UI
                            _userInfo.postValue(it)
                        }
                    }
                }
            } catch (e: Exception) {
                // 捕获网络异常：无网络、超时、服务器500等
                e.printStackTrace()
            }
        }
    }
}