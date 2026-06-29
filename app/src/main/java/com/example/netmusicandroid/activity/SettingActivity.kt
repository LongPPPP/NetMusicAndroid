package com.example.netmusicandroid.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netmusicandroid.R
import com.example.netmusicandroid.databinding.ActivitySettingBinding
import com.example.netmusicandroid.dialog.EditProfileDialog
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SettingViewModel
import com.example.netmusicandroid.utils.ToastUtil
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var settingVm: SettingViewModel
    private lateinit var bottomVm: BottomPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initObserver()
        initClick()
        initBottomPlayer()

        // 预加载当前用户数据供弹窗使用
        settingVm.loadCurrentUser()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomVm.isInitialized) bottomVm.syncPlayState()
    }

    private fun initViewModel() {
        settingVm = ViewModelProvider(this)[SettingViewModel::class.java]
        bottomVm = ViewModelProvider(this)[BottomPlayerViewModel::class.java]
    }

    // ── 底部播放栏 ──────────────────────────────

    private fun initBottomPlayer() {
        val bp = binding.includeBottomPlayer
        bottomVm.songName.observe(this) { bp.tvSongName.text = it }
        bottomVm.singerName.observe(this) { bp.tvSinger.text = it }
        bottomVm.coverUrl.observe(this) { url ->
            if (!url.isNullOrEmpty()) ImageLoadUtil.loadImage(bp.ivSongCover, url)
        }
        bottomVm.hasCurrentSong.observe(this) { has ->
            bp.root.visibility = if (has) View.VISIBLE else View.GONE
        }
        bottomVm.isPlaying.observe(this) { playing ->
            bp.ivPlayToggle.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
            )
        }
        bottomVm.toastMsg.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                ToastUtil.showShort(msg)
                bottomVm.clearToast()
            }
        }
        bp.ivPrev.setOnClickListener { bottomVm.playPrev() }
        bp.ivPlayToggle.setOnClickListener { bottomVm.togglePlayPause() }
        bp.ivNext.setOnClickListener { bottomVm.playNext() }

        val goPlayer = View.OnClickListener { BaseActivity.navigateToPlayerFrom(this) }
        bp.cvCover.setOnClickListener(goPlayer)
        bp.llSongInfo.setOnClickListener(goPlayer)
    }

    private fun initObserver() {
        // 监听吐司消息
        settingVm.toastMsg.observe(this) { msg ->
            ToastUtil.showShort(msg)
        }
    }

    private fun initClick() {
        // 返回上一页
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 修改个人信息 → 弹出 EditProfileDialog
        binding.rlItemInfo.setOnClickListener {
            val user = settingVm.currentUser.value
            if (user == null) {
                ToastUtil.showShort("请先登录")
                return@setOnClickListener
            }
            EditProfileDialog(this, user) { field, value ->
                settingVm.updateUserField(field, value)
            }.show()
        }

        // 退出登录（suspend函数需协程包裹调用）
        binding.rlItemLogout.setOnClickListener {
            lifecycleScope.launch {
                settingVm.logoutAction()
                ToastUtil.showShort("已退出登录")
                // 直接调用静态方法，取消强转BaseActivity
                BaseActivity.globalGoLogin()
                finish()
            }
        }

        // 修改主题
        binding.rlItemTheme.setOnClickListener {
            settingVm.goModifyTheme()
        }
    }
}