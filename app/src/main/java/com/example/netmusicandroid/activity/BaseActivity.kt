package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.netmusicandroid.MinMusicApp
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.databinding.ActivityBaseBinding
import com.example.netmusicandroid.fragment.HomeFragment
import com.example.netmusicandroid.fragment.MineFragment
import com.example.netmusicandroid.fragment.PlayerFragment
import kotlinx.coroutines.runBlocking

/**
 * 应用主底部导航容器Activity
 * 承载首页、播放器、我的三个Fragment，全局共享MainViewModel，提供页面跳转、登录退出、前台状态判断工具方法
 */
class BaseActivity : AppCompatActivity() {
    // 全局共享ViewModel，全应用播放歌曲状态统一管理
    // 页面ViewBinding
    private lateinit var binding: ActivityBaseBinding

    companion object {
        // Intent标记：从外部页面跳转至播放页
        const val EXTRA_NAV_TO_PLAYER = "nav_to_player"

        // 标记App是否处于前台可见状态
        private var isForeground = false

        /** 判断应用当前是否在前台 */
        fun isAppForeground(): Boolean = isForeground

        /**
         * 静态工具方法：其他独立页面跳转回主界面并打开全屏播放器
         * 清空顶部重复页面，复用已有BaseActivity
         */
        fun navigateToPlayerFrom(context: android.content.Context) {
            val intent = Intent(context, BaseActivity::class.java).apply {
                putExtra(EXTRA_NAV_TO_PLAYER, true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }

        /**
         * 全局静态登出方法
         * 无Context场景（网络拦截器Token失效）调用，清除登录状态并跳转登录页
         */
        fun globalGoLogin() {
            val context = MinMusicApp.globalContext
            // 同步执行登出数据库操作
            runBlocking {
                AuthRepository.getInstance().logout()
            }
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // 页面切前台，标记应用前台状态
    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    // 页面切后台，标记应用后台状态
    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化Activity作用域共享VM

        // 首次创建加载首页Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // 接收外部跳转标记，自动切换到播放页面
        if (intent.getBooleanExtra(EXTRA_NAV_TO_PLAYER, false)) {
            navigateToPlayer()
        }

        // 底部导航栏切换监听
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.menu_play -> {
                    replaceFragment(PlayerFragment())
                    true
                }
                R.id.menu_mine -> {
                    replaceFragment(MineFragment())
                    true
                }
                else -> false
            }
        }
    }

    // ---------------- Fragment导航工具方法 ----------------

    /** 给Fragment调用，自动选中底部播放Tab并切换播放器Fragment */
    fun navigateToPlayer() {
        binding.bottomNav.selectedItemId = R.id.menu_play
    }

    /** 替换容器内当前Fragment */
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}