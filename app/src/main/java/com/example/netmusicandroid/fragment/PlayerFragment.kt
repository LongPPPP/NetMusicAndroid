package com.example.netmusicandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.netmusicandroid.R

class PlayerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ★★★ 把你原来写在 Activity 的 onCreate() 里的代码全部复制到这里 ★★★
        // 但需要做 3 处替换：
        // 1. findViewById → 全部改成 view.findViewById
        // 2. Toast.makeText(this, ...) → Toast.makeText(requireContext(), ...)
        // 3. 任何用 this 的地方，如果是 Context，改成 requireContext()；如果是 Activity，改成 requireActivity()

        // 示例：播放按钮点击
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        btnPlay.setOnClickListener {
            // 这里写你的播放/暂停逻辑
            Toast.makeText(requireContext(), "播放按钮点击", Toast.LENGTH_SHORT).show()
        }


    }
}