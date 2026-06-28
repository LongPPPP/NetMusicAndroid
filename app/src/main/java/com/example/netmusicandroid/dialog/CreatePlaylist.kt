// dialog/CreatePlaylistDialog.kt
package com.example.netmusicandroid.dialog

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.netmusicandroid.R
import com.example.netmusicandroid.databinding.DialogCreatePlaylistBinding

class CreatePlaylistDialog(
    private val context: Context,
    private val onConfirm: (String) -> Unit   // 回调：返回歌单名称
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogCreatePlaylistBinding.inflate(LayoutInflater.from(context))

    fun show() {
        // 清空输入，重置字数
        binding.etName.text.clear()
        binding.tvCount.text = "0/30"

        // 实时字数监听
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val len = s?.length ?: 0
                binding.tvCount.text = "$len/30"
            }
        })

        // 关闭按钮
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // 确认创建：回调返回输入内容
        binding.btnConfirm.setOnClickListener {
            val inputName = binding.etName.text.toString().trim()
            if (inputName.isNotEmpty()) {
                onConfirm(inputName)
                dismiss()
            }
        }

        // 创建Dialog
        dialog = AlertDialog.Builder(context, R.style.FullTransDialogStyle)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        dialog?.show()

        // ========== 核心修复：强制设置弹窗宽度占屏幕90%，解决弹窗过窄问题 ==========
        dialog?.window?.let { window ->
            val layoutParams = window.attributes
            // 获取屏幕宽度
            val screenWidth = context.resources.displayMetrics.widthPixels
            // 设置弹窗宽度为屏幕90%，高度自适应
            layoutParams.width = (screenWidth * 0.9).toInt()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}