package com.example.netmusicandroid.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.data.model.bean.UserCollectionBean
import com.example.netmusicandroid.databinding.ItemUserCollectionBinding
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class UserCollectionAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<UserCollectionBean, UserCollectionAdapter.CollectionVH>(CollectionDiffCallback()) {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    inner class CollectionVH(val binding: ItemUserCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserCollectionBean) {
            // 文本赋值
            binding.tvCollectionName.text = item.collectionName
            binding.tvSongNum.text = "${item.songCount}首"
            // 原生加载网络封面，无Glide
            loadCircleCover(binding.ivCover, item.coverUrl)
            // 删除点击
            binding.ivDelete.setOnClickListener {
                onDeleteClick.invoke(item.collectionId)
            }
        }

        // 原生网络图片+圆形裁剪
        private fun loadCircleCover(iv: android.widget.ImageView, urlStr: String) {
            executor.submit {
                var sourceBit: Bitmap? = null
                try {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    val input: InputStream = conn.inputStream
                    sourceBit = BitmapFactory.decodeStream(input)
                    input.close()
                    conn.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val finalBit = sourceBit
                mainHandler.post {
                    finalBit?.let {
                        val circleBit = getCircleBitmap(it)
                        iv.setImageBitmap(circleBit)
                    }
                }
            }
        }

        // 生成圆形Bitmap
        private fun getCircleBitmap(source: Bitmap): Bitmap {
            val minSize = minOf(source.width, source.height)
            val output = Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            }
            val rect = Rect(0, 0, minSize, minSize)
            canvas.drawOval(RectF(rect), paint)
            canvas.drawBitmap(source, rect, rect, paint)
            if (!source.isRecycled) source.recycle()
            return output
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionVH {
        val bind = ItemUserCollectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CollectionVH(bind)
    }

    override fun onBindViewHolder(holder: CollectionVH, position: Int) {
        holder.bind(getItem(position))
    }

    class CollectionDiffCallback : DiffUtil.ItemCallback<UserCollectionBean>() {
        override fun areItemsTheSame(oldItem: UserCollectionBean, newItem: UserCollectionBean): Boolean {
            return oldItem.collectionId == newItem.collectionId
        }

        override fun areContentsTheSame(oldItem: UserCollectionBean, newItem: UserCollectionBean): Boolean {
            return oldItem == newItem
        }
    }
}