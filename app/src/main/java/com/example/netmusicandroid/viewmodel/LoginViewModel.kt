//suspend函数只能在协程里面调用,所以要launch开启一个协程
package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginViewModel:ViewModel(){
    // 复用全局初始化好的Dao，不重复创建数据库
    private val userDao = AppDatabase.globalUserDao
    // 使用单例获取AuthRepository，不再new构造
    private val r = AuthRepository.getInstance()

    fun login(e:String,p:String,cb:(Boolean,String)->Unit){
        viewModelScope.launch{
            try{
                val res=r.login(e,p)
                if (res.code == 200 && res.data != null) {
                    // 仓库内部已经执行setUserId、登录状态、本地User存储
                }
                cb(res.code==200,res.message?:"")
            }catch(ex:HttpException){
                val msg=try{
                    JSONObject(ex.response()?.errorBody()?.string()?:"").getString("message")
                }catch(e:Exception){
                    "登录失败"
                }
                cb(false,msg)
            }catch(e:Exception){
                cb(false,e.message?:"网络异常")
            }
        }
    }

    // 获取所有历史登录用户
    fun getAllHistoryUsers(callback: (List<UserEntity>) -> Unit) {
        viewModelScope.launch {
            callback(userDao.getAllHistoryUsers())
        }
    }

    // 从本地删除某个账号
    fun deleteLocalUser(user: UserEntity, callback: () -> Unit) {
        viewModelScope.launch {
            userDao.deleteUser(user)
            callback()
        }
    }

    // 获取最近一次登录的用户
    fun getLastUser(callback: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val users = userDao.getAllHistoryUsers()
            if (users.isNotEmpty()) {
                callback(users[0]) // 按时间倒序排，第一个就是最近的
            } else {
                callback(null)
            }
        }
    }
}