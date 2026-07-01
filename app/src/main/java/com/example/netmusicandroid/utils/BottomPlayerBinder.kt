package com.example.netmusicandroid.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.netmusicandroid.R
import com.example.netmusicandroid.activity.BaseActivity
import com.example.netmusicandroid.databinding.LayoutBottomPlayerBinding
import com.example.netmusicandroid.viewmodel.BottomPlayerViewModel

object BottomPlayerBinder {
    fun bind(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        binding: LayoutBottomPlayerBinding,
        bottomVm: BottomPlayerViewModel,
        onOpenPlayer: (() -> Unit)? = null
    ) {
        binding.run {
            val coverRotateAnimator = ObjectAnimator.ofFloat(cvCover, View.ROTATION, 0f, 360f).apply {
                duration = 12000L
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }

            bottomVm.songName.observe(lifecycleOwner) { tvSongName.text = it }
            bottomVm.singerName.observe(lifecycleOwner) { tvSinger.text = it }
            bottomVm.coverUrl.observe(lifecycleOwner) { url ->
                ImageLoadUtil.loadImage(ivSongCover, MusicPlayerManager.resolveUrl(url))
            }
            bottomVm.hasCurrentSong.observe(lifecycleOwner) { has ->
                root.visibility = if (has) View.VISIBLE else View.GONE
            }
            bottomVm.isPlaying.observe(lifecycleOwner) { playing ->
                ivPlayToggle.setImageResource(
                    if (playing) R.drawable.ic_pause else R.drawable.ic_play_triangle
                )
                if (playing) {
                    if (!coverRotateAnimator.isStarted) {
                        coverRotateAnimator.start()
                    } else {
                        coverRotateAnimator.resume()
                    }
                } else if (coverRotateAnimator.isStarted) {
                    coverRotateAnimator.pause()
                }
            }
            bottomVm.toastMsg.observe(lifecycleOwner) { msg ->
                if (msg.isNotEmpty()) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    bottomVm.clearToast()
                }
            }

            ivPrev.setOnClickListener { bottomVm.playPrev() }
            ivPlayToggle.setOnClickListener { bottomVm.togglePlayPause() }
            ivNext.setOnClickListener { bottomVm.playNext() }

            val goPlayer = View.OnClickListener {
                onOpenPlayer?.invoke() ?: BaseActivity.navigateToPlayerFrom(context)
            }
            cvCover.setOnClickListener(goPlayer)
            llSongInfo.setOnClickListener(goPlayer)
        }
    }
}
