package com.example.netmusicandroid.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.db.UserEntity
import com.example.netmusicandroid.databinding.DialogEditProfileBinding
import com.example.netmusicandroid.utils.ImageLoadUtil

/**
 * 修改个人信息弹窗 — 复用项目 CreatePlaylistDialog 的 AlertDialog + FullTransDialogStyle 模式。
 *
 * @param currentUser 当前登录用户数据（预填昵称/签名/头像）
 * @param onSave 回调：(field: String, value: String) → 调用 ViewModel 执行 PATCH /users/me
 */
class EditProfileDialog(
    private val context: Context,
    private val currentUser: UserEntity,
    private val onSave: (field: String, value: String) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))

    fun show() {
        // 预填当前数据
        binding.etNickname.setText(currentUser.username)
        binding.etSignature.setText(currentUser.signature)
        binding.etAvatarUrl.setText(currentUser.avatar)
        ImageLoadUtil.loadImage(binding.ivAvatarPreview, currentUser.avatar)

        // 关闭
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // 保存：逐个比较字段，有变更则回调
        binding.btnSave.setOnClickListener {
            val newAvatar = binding.etAvatarUrl.text.toString().trim()
            val newNick = binding.etNickname.text.toString().trim()
            val newSig = binding.etSignature.text.toString().trim()

            if (newAvatar.isNotEmpty() && newAvatar != currentUser.avatar) {
                onSave("avatar", newAvatar)
            }
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

        // 宽度占屏幕 90%
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
