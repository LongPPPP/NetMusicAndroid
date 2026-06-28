package com.example.netmusicandroid.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.fragment.HomeFragment
import com.example.netmusicandroid.fragment.MineFragment
import com.example.netmusicandroid.fragment.PlayerFragment

import androidx.lifecycle.ViewModelProvider
import com.example.netmusicandroid.viewmodel.MainViewModel

class BaseActivity : AppCompatActivity() {

    lateinit var mainViewModel: MainViewModel
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // 初始化共享 ViewModel
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 默认显示首页
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // 底部导航切换
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { menuItem ->
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

    // 提供给 Fragment 调用：跳转到播放页并选中底部按钮
    fun navigateToPlayer() {
        bottomNav.selectedItemId = R.id.menu_play
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
