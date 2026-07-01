package com.example.netmusicandroid.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netmusicandroid.R
import com.example.netmusicandroid.databinding.ActivitySettingBinding
import com.example.netmusicandroid.dialog.EditProfileDialog
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SettingViewModel
import com.example.netmusicandroid.utils.ToastUtil
import kotlinx.coroutines.launch
import java.io.File

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var settingVm: SettingViewModel
    private lateinit var bottomVm: BottomPlayerViewModel
    private var editDialog: EditProfileDialog? = null
    // 缓存相册选中的临时头像Uri，点击保存才处理上传
    private var pendingAvatarUri: Uri? = null

    // 相册选择：仅缓存Uri、弹窗预览，不立即上传
    private val pickAvatarLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            pendingAvatarUri = it
            editDialog?.setAvatarUri(it)
        }
    }

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
            ImageLoadUtil.loadImage(bp.ivSongCover, MusicPlayerManager.resolveUrl(url))
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
            // 打开弹窗前清空上次缓存的头像
            pendingAvatarUri = null
            editDialog = EditProfileDialog(
                this, user,
                onPickAvatar = { pickAvatarLauncher.launch("image/*") },
                // 适配新弹窗回调：接收昵称、签名、临时头像Uri
                onSaveProfile = { newNick, newSig, tempAvatarUri ->
                    lifecycleScope.launch {
                        // 1. 如果有新头像，先转临时文件上传
                        tempAvatarUri?.let { uri ->
                            val tempFile = File(cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
                            contentResolver.openInputStream(uri)?.use { input ->
                                tempFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            // 上传头像，接口内部更新用户avatar字段
                            settingVm.uploadAvatar(tempFile)
                        }

                        // 2. 分别更新昵称/签名（不为null代表有修改）
                        newNick?.let { settingVm.updateUserField("username", it) }
                        newSig?.let { settingVm.updateUserField("signature", it) }
                    }
                }
            )
            editDialog!!.show()
        }

        // 退出登录（suspend函数需协程包裹调用）
        binding.rlItemLogout.setOnClickListener {
            lifecycleScope.launch {
                MusicPlayerManager.stop()
                settingVm.logoutAction()
                ToastUtil.showShort("已退出登录")
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