package com.example.netmusicandroid.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netmusicandroid.databinding.ActivitySettingBinding
import com.example.netmusicandroid.dialog.EditProfileDialog
import com.example.netmusicandroid.utils.BottomPlayerBinder
import com.example.netmusicandroid.utils.MusicPlayerManager
import com.example.netmusicandroid.utils.ToastUtil
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel
import com.example.netmusicandroid.viewmodel.SettingViewModel
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
        BottomPlayerBinder.bind(this, this, binding.includeBottomPlayer, bottomVm)
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
                        // 1. 先上传头像（suspend，等待完成再继续，避免与后续 updateUserField 并发导致 Room 写入竞态）
                        tempAvatarUri?.let { uri ->
                            val tempFile = File(cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
                            contentResolver.openInputStream(uri)?.use { input ->
                                tempFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            settingVm.uploadAvatar(tempFile)
                        }

                        // 2. 再更新昵称（串行执行，保证 Room 中头像已是新值后再写用户名）
                        newNick?.let { settingVm.updateUserField("username", it) }

                        // 3. 最后更新签名
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