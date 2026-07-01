package com.example.netmusicandroid.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.netmusicandroid.R
import com.example.netmusicandroid.data.repository.SongRepository
import com.example.netmusicandroid.utils.FileUtil
import kotlinx.coroutines.launch
import java.io.File

class UploadSongActivity : AppCompatActivity() {

    private val repository = SongRepository.getInstance()
    private var coverUri: Uri? = null
    private var songUri: Uri? = null

    private lateinit var tvCoverPath: TextView
    private lateinit var tvSongPath: TextView
    private lateinit var progressBar: ProgressBar

    private val pickCoverLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            coverUri = it
            tvCoverPath.text = "已选封面"
        }
    }

    private val pickSongLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            songUri = it
            tvSongPath.text = "已选歌曲文件"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_song)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        val etName = findViewById<EditText>(R.id.etSongName)
        tvCoverPath = findViewById(R.id.tvCoverPath)
        tvSongPath = findViewById(R.id.tvSongPath)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.btnPickCover).setOnClickListener {
            pickCoverLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnPickSong).setOnClickListener {
            pickSongLauncher.launch("*/*") // 扩大范围，防止有些音频被过滤
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "请输入歌曲名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (songUri == null) {
                Toast.makeText(this, "请选择歌曲文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            upload(name)
        }
    }

    private fun upload(name: String) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                // 【核心优化】：使用标准文件名，规避后端字符编码崩溃
                val songFile = FileUtil.uriToFile(this@UploadSongActivity, songUri!!, "temp_music_file.mp3")
                val coverFile = coverUri?.let {
                    FileUtil.uriToFile(this@UploadSongActivity, it, "temp_cover_file.jpg")
                }

                val result = repository.publishSong(name, coverFile, songFile)
                
                result.onSuccess {
                    Toast.makeText(this@UploadSongActivity, "上架成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure {
                    // 打印详细错误
                    println("UploadError: ${it.message}")
                    Toast.makeText(this@UploadSongActivity, "失败: ${it.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                println("UploadFatalError: ${e.stackTraceToString()}")
                Toast.makeText(this@UploadSongActivity, "文件处理致命错误", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
