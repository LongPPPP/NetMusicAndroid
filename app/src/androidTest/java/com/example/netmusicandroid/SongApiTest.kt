package com.example.netmusicandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 极简接口测试：获取歌曲 URL 并播放。
 *
 * 前置条件：
 * 1. 后端服务运行在 http://localhost:3000（默认端口）
 * 2. 至少有一首歌曲数据（seed 或手动添加）
 *
 * 注意：Android 模拟器中 10.0.2.2 映射到宿主机 localhost。
 */
@RunWith(AndroidJUnit4::class)
class SongApiTest {

    private val baseUrl = "http://10.0.2.2:3000/api/v1"

    @Before
    fun checkNetwork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull("Context should not be null", context)
    }

    @Test
    fun testFetchSongsAndPlay() {
        // 1. GET /songs 获取歌曲列表
        val songsJson = httpGet("$baseUrl/songs?page=1&page_size=5")
        assertNotNull("Response should not be null", songsJson)
        println("=== 歌曲列表响应 ===")
        println(songsJson)

        // 解析 JSON
        val response = org.json.JSONObject(songsJson)
        assertEquals("code should be 200", 200, response.getInt("code"))

        val data = response.getJSONObject("data")
        val list = data.getJSONArray("list")
        assertTrue("Song list should not be empty", list.length() > 0)

        // 取第一首歌曲
        val firstSong = list.getJSONObject(0)
        val songId = firstSong.getInt("song_id")
        val songName = firstSong.getString("song_name")
        val singerName = firstSong.getString("singer_name")
        val playUrl = firstSong.optString("play_url", null)

        println("第一首歌: $songName - $singerName (ID=$songId) play_url=$playUrl")

        // 2. 如果列表中没有 play_url，尝试通过详情接口获取
        var finalPlayUrl = playUrl
        if (finalPlayUrl.isNullOrEmpty()) {
            println("列表无 play_url，尝试 GET /songs/$songId")
            val detailJson = httpGet("$baseUrl/songs/$songId")
            println("=== 歌曲详情响应 ===")
            println(detailJson)

            val detailResponse = org.json.JSONObject(detailJson)
            assertEquals(200, detailResponse.getInt("code"))
            val songDetail = detailResponse.getJSONObject("data")
            finalPlayUrl = songDetail.optString("play_url", null)
            println("详情 play_url: $finalPlayUrl")
        }

        // 3. 断言存在 play_url
        assertNotNull("play_url should not be null", finalPlayUrl)
        assertFalse("play_url should not be empty", finalPlayUrl!!.isEmpty())

        // 4. 构建完整 URL 并尝试播放
        val fullUrl = if (finalPlayUrl.startsWith("http")) finalPlayUrl
                      else "http://10.0.2.2:3000$finalPlayUrl"
        println("完整播放 URL: $fullUrl")

        // 5. 用 MediaPlayer 播放 5 秒
        val mediaPlayer = android.media.MediaPlayer().apply {
            setDataSource(fullUrl)
            setOnPreparedListener {
                println("=== 开始播放: $songName ===")
                start()
            }
            setOnErrorListener { _, what, extra ->
                println("=== 播放失败: what=$what extra=$extra ===")
                false
            }
            prepareAsync()
        }

        // 等待播放 5 秒
        Thread.sleep(5000)

        if (mediaPlayer.isPlaying) {
            println("=== 播放中, 停止 ===")
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        println("=== 测试完成 ===")
    }

    /** 使用 HttpURLConnection 发送 GET 请求 */
    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        return try {
            val code = conn.responseCode
            println("HTTP $code: $urlString")
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            BufferedReader(InputStreamReader(stream)).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
