package com.example.netmusicandroid.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.netmusicandroid.MinMusicApp
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.repository.AuthRepository
import com.example.netmusicandroid.databinding.ActivityBaseBinding
import com.example.netmusicandroid.fragment.HomeFragment
import com.example.netmusicandroid.fragment.MineFragment
import com.example.netmusicandroid.fragment.PlayerFragment
import com.example.netmusicandroid.viewmodel.MainViewModel
import kotlinx.coroutines.runBlocking

class BaseActivity : AppCompatActivity() {

    lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityBaseBinding

    companion object {
        const val EXTRA_NAV_TO_PLAYER = "nav_to_player"

        private var isForeground = false

        fun isAppForeground(): Boolean = isForeground

        /**
         * 供独立 Activity（Search/PlaylistDetail 等）从底部迷你播放栏跳转到全屏播放页。
         * 启动 BaseActivity 并携带标记，使其自动切换到 PlayerFragment 标签页。
         */
        fun navigateToPlayerFrom(context: android.content.Context) {
            val intent = Intent(context, BaseActivity::class.java).apply {
                putExtra(EXTRA_NAV_TO_PLAYER, true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }

        /**
         * 全局静态方法：供 OkHttp 拦截器无Context场景调用（Token刷新失败强制登出）
         */
        fun globalGoLogin() {
            val context = MinMusicApp.globalContext
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

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化共享 ViewModel
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 默认显示首页
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // 从独立 Activity（搜索/歌单详情等）的迷你播放栏跳转 → 自动切到播放页
        if (intent.getBooleanExtra(EXTRA_NAV_TO_PLAYER, false)) {
            navigateToPlayer()
        }

        // 底部导航切换
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

    // ── Fragment & 导航 ─────────────────────────

    /** 提供给 Fragment 调用：跳转到播放页并选中底部按钮 */
    fun navigateToPlayer() {
        binding.bottomNav.selectedItemId = R.id.menu_play
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * 实例方法：当前页面直接跳转登录，关闭所有Activity
     */
    fun goLogin() {
        runBlocking {
            AuthRepository.getInstance().logout()
        }
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
