package com.example.netmusicandroid.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netmusicandroid.R
import com.example.netmusicandroid.adapter.CommentAdapter
import com.example.netmusicandroid.viewmodel.CommentViewModel

class CommentActivity : AppCompatActivity() {

    private lateinit var viewModel: CommentViewModel
    private lateinit var adapter: CommentAdapter
    private var songId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)

        // 处理沉浸式状态栏/导航栏遮挡问题
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 获取从播放页面传过来的歌曲ID
        songId = intent.getIntExtra("SONG_ID", -1)
        if (songId == -1) {
            Toast.makeText(this, "无效的歌曲ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. 初始化布局控件
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnBack = findViewById<ImageView>(R.id.iv_back)

        // 3. 设置列表适配器
        adapter = CommentAdapter(emptyList()) { comment ->
            // 这里处理删除逻辑
            viewModel.deleteComment(songId, comment.comment_id) { success, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = adapter

        // 4. 初始化 ViewModel
        viewModel = ViewModelProvider(this)[CommentViewModel::class.java]

        // 5. 观察数据变化
        viewModel.comments.observe(this) { list ->
            // 当评论列表更新时，通知适配器刷新
            adapter.updateData(list)
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // 6. 按钮点击事件
        btnBack.setOnClickListener { finish() }

        btnSend.setOnClickListener {
            val content = etComment.text.toString().trim()
            viewModel.sendComment(songId, content) { success, msg ->
                if (success) {
                    etComment.setText("") // 发送成功清空输入框
                    Toast.makeText(this, "发表成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 7. 首次进入页面，加载评论列表
        viewModel.loadComments(songId, isRefresh = true)
    }
}
