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
 * 修改个人信息弹窗 — 头像从本地文件选择，昵称/签名文本编辑。
 *
 * @param currentUser 当前登录用户数据
 * @param onPickAvatar 回调：用户点击"选择头像" → 外部启动文件选择器
 * @param onSave 回调：(field: String, value: String) → PATCH /users/me 修改昵称/签名
 */
class EditProfileDialog(
    private val context: Context,
    private val currentUser: UserEntity,
    private val onPickAvatar: () -> Unit,
    private val onSave: (field: String, value: String) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))

    /** 外部文件选择器回调后调用，更新预览 */
    fun setAvatarUri(uri: Uri) {
        binding.ivAvatarPreview.setImageURI(uri)
    }

    fun show() {
        // 预填当前数据
        binding.etNickname.setText(currentUser.username)
        binding.etSignature.setText(currentUser.signature)
        ImageLoadUtil.loadImage(
            binding.ivAvatarPreview,
            MusicPlayerManager.resolveUrl(currentUser.avatar)
        )

        // 关闭
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // 点击选择头像 → 通知外部打开文件选择器
        binding.btnPickAvatar.setOnClickListener { onPickAvatar() }

        // 保存：昵称/签名变更则回调
        binding.btnSave.setOnClickListener {
            val newNick = binding.etNickname.text.toString().trim()
            val newSig = binding.etSignature.text.toString().trim()

            if (newNick.isNotEmpty() && newNick != currentUser.username) {
                onSave("username", newNick)
            }
            if (newSig != currentUser.signature) {
                onSave("signature", newSig)
            }
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