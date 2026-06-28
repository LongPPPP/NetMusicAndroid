//suspend函数只能在协程里面调用,所以要lunch开启一个协程
package com.example.netmusicandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netmusicandroid.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

import com.example.netmusicandroid.MinMusicApp
import com.example.netmusicandroid.data.db.AppDatabase
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.sp.SpManager

class LoginViewModel:ViewModel(){

    private val r=AuthRepository()
    private val db = AppDatabase.getDatabase(MinMusicApp.globalContext)

    fun login(e:String,p:String,cb:(Boolean,String)->Unit){
        viewModelScope.launch{
            try{
                val res=r.login(e,p)
                if (res.code == 200 && res.data != null) {
                    // 1. 保存 Token 到 SharedPreferences (用于接口鉴权)
                    SpManager.setToken(res.data.access_token)
                    SpManager.setUserId(res.data.user_id.toLong())
                    SpManager.setLoginStatus(true)

                    // 2. 保存账号密码到本地 Room 数据库 (用于记住密码/自动填充)
                    val userEntity = UserEntity(
                        email = e,
                        username = res.data.user.username,
                        password = p, // 明文存储方便演示记住密码
                        lastLoginTime = System.currentTimeMillis()
                    )
                    db.userDao().saveUser(userEntity)
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
            callback(db.userDao().getAllHistoryUsers())
        }
    }

    // 从本地删除某个账号
    fun deleteLocalUser(user: UserEntity, callback: () -> Unit) {
        viewModelScope.launch {
            db.userDao().deleteUser(user)
            callback()
        }
    }

    // 获取最近一次登录的用户
    fun getLastUser(callback: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val users = db.userDao().getAllHistoryUsers()
            if (users.isNotEmpty()) {
                callback(users[0]) // 按时间倒序排，第一个就是最近的
            } else {
                callback(null)
            }
        }
    }
}