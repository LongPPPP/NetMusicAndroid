package com.example.netmusicandroid.dialog

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.databinding.DialogEditProfileBinding
import com.example.netmusicandroid.utils.ImageLoadUtil
import com.example.netmusicandroid.utils.MusicPlayerManager

/**
 * 修改个人信息弹窗 — 头像本地预览仅临时生效，点击保存才提交更新；昵称/签名文本编辑。
 *
 * @param currentUser 当前登录用户原始数据
 * @param onPickAvatar 回调：用户点击"选择头像" → 外部启动文件选择器
 * @param onSaveProfile 保存回调：(newName:String?, newSig:String?, avatarLocalUri:Uri?)
 *      外部处理：头像上传接口 + 昵称/签名PATCH接口，全部成功后刷新用户数据
 */
class EditProfileDialog(
    private val context: Context,
    private val currentUser: UserEntity,
    private val onPickAvatar: () -> Unit,
    // 修改回调参数：新增临时选中的头像Uri，为空代表本次未更换头像
    private val onSaveProfile: (newNick: String?, newSig: String?, tempAvatarUri: Uri?) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))
    // 缓存本次临时选中的本地头像Uri，仅点击保存才提交，取消则丢弃
    private var tempSelectedAvatarUri: Uri? = null

    /** 外部文件选择器回调后，仅更新弹窗内临时预览，不持久保存 */
    fun setAvatarUri(uri: Uri) {
        tempSelectedAvatarUri = uri
        binding.ivAvatarPreview.setImageURI(uri)
    }

    fun show() {
        // 弹窗打开重置临时头像缓存，加载原始用户头像
        tempSelectedAvatarUri = null
        binding.etNickname.setText(currentUser.username)
        binding.etSignature.setText(currentUser.signature)
        ImageLoadUtil.loadImage(
            binding.ivAvatarPreview,
            MusicPlayerManager.resolveUrl(currentUser.avatar)
        )

        // 关闭/取消：丢弃本次选中的临时头像，不提交任何修改
        val cancelAction = {
            tempSelectedAvatarUri = null
            dismiss()
        }
        binding.ivClose.setOnClickListener { cancelAction() }
        binding.btnCancel.setOnClickListener { cancelAction() }

        // 点击选择头像 → 通知外部打开文件选择器
        binding.btnPickAvatar.setOnClickListener { onPickAvatar() }

        // 保存按钮：统一把昵称、签名、临时头像Uri传给外部处理上传+接口更新
        binding.btnSave.setOnClickListener {
            val newNick = binding.etNickname.text.toString().trim()
            val newSig = binding.etSignature.text.toString().trim()

            // 过滤无修改的字段，传null表示不更新该字段
            val submitNick = if (newNick.isNotEmpty() && newNick != currentUser.username) newNick else null
            val submitSig = if (newSig != currentUser.signature) newSig else null

            // 回调交给外部执行：头像上传、用户信息PATCH接口
            onSaveProfile(submitNick, submitSig, tempSelectedAvatarUri)
            dismiss()
        }

        dialog = AlertDialog.Builder(context, R.style.FullTransDialogStyle)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        dialog?.show()

        dialog?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}