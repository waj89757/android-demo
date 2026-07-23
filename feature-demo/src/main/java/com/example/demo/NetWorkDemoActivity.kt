package com.example.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.network.Post
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ★ NetworkDemoActivity —— 演示 Retrofit 网络请求
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  网络请求的完整流程（★ 必须遵守的规则）                        │
 * │                                                             │
 * │  ① 用户点击按钮 → 触发网络请求（主线程）                       │
 * │ ② lifecycleScope.launch → 启动协程（主线程上启动）             │
 * │ ③ withContext(Dispatchers.IO) → 切到IO子线程发请求            │
 * │    ★ 这是关键！网络请求必须在IO线程，不能在主线程               │
 * │ ④ Retrofit 发 HTTP请求 → 等待后端响应（IO线程，不卡UI）        │
 * │ ⑤ 拿到响应数据 → 自动切回主线程（协程自动切换）                │
 * │ ⑥ 更新UI（setText等）→ 主线程，安全！                         │
 * │                                                             │
 * │  ★ 协程的本质：                              │
 * │    协程不是线程！它比线程更轻量，就像"可暂停的函数"              │
 * │    withContext(IO) → 函数暂停，切到IO线程执行                  │
 * │    执行完 → 自动切回主线程，继续执行后面的代码                   │
 * │    从代码上看像同步代码，实际执行是异步的                        │
 * │                                                             │
 * │  对比后端：                                                  │
 * │    Python:  async def fetch(): await aiohttp.get(...)        │
 * │    Kotlin:  suspend fun fetch(): withContext(IO) { api.get } │
 * │    写法几乎一样！都是 async/await 模式                        │
 * └─────────────────────────────────────────────────────────────┘
 */
class NetworkDemoActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnFetch: Button
    private lateinit var btnBackMain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_demo)

        tvStatus = findViewById(R.id.tv_status)
        tvResult = findViewById(R.id.tv_result)
        btnFetch = findViewById(R.id.btn_fetch)
        btnBackMain = findViewById(R.id.btn_back_main)

        btnFetch.setOnClickListener { fetchPosts() }
        btnBackMain.setOnClickListener { finish() }
    }

    /**
     * ★ 核心方法：发起网络请求
     *
     * 流程详解：
     * lifecycleScope.launch { ... }  → 在主线程上启动一个协程
     *   里面的代码按顺序写，看起来像同步代码，但实际是异步执行
     *
     * withContext(Dispatchers.IO) { ... }  → 切到IO子线程执行
     *   ★ 网络请求必须在这里！
     *   执行完后，协程自动切回主线程
     *
     * 协程切回主线程后，后面的代码（tvResult.text = ...）在主线程执行
     *   ★ 所以可以安全地更新UI！
     *
     * 后端类比：
     *   Python async:
     *     async def main():
     *         status = "请求中..."
     *         data = await fetch_from_api()   # ← 暂停，异步等待
     *         result = format(data)            # ← 恢复，继续执行
     *
     *   Kotlin coroutine:
     *     lifecycleScope.launch {
     *         status = "请求中..."
     *         data = withContext(IO) { api.get() }  # ← 暂停，切到IO线程
     *         result.text = format(data)             # ← 恢复，回到主线程
     *     }
     */
    private fun fetchPosts() {
        // ★ 步骤①：UI 提示"正在请求"（主线程）
        tvStatus.text = "正在请求 https://jsonplaceholder.typicode.com/posts ..."
        tvResult.text = "加载中..."
        btnFetch.isEnabled = false  // 防止重复点击

        // ★ 步骤②：启动协程，发起异步网络请求
        lifecycleScope.launch {

            try {
                // ★ 步骤③④：切到IO子线程，发网络请求
                // withContext(Dispatchers.IO) = "这段代码在IO线程执行"
                // Retrofit 的 suspend 函数在IO线程安全执行，不会卡UI
                val posts = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getPosts(5)
                }

                // ★ 步骤⑤⑥：自动切回主线程，更新UI
                // withContext 结束后，协程自动回到主线程
                // 所以这里可以直接操作UI，不会报错
                tvStatus.text = "请求成功！获取到 ${posts.size} 条数据"
                displayPosts(posts)
                btnFetch.isEnabled = true

            } catch (e: Exception) {
                // ★ 网络请求失败的处理（也在主线程）
                // 可能的错误：网络断开、服务器超时、JSON解析失败等
                tvStatus.text = "请求失败：${e.message}"
                tvResult.text = "错误详情：\n${e.stackTraceToString()}"
                btnFetch.isEnabled = true
                Toast.makeText(this@NetworkDemoActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 把请求结果格式化展示到屏幕上
    private fun displayPosts(posts: List<Post>) {
        val builder = StringBuilder()
        builder.append("★ API: jsonplaceholder.typicode.com/posts?_limit=5\n")
        builder.append("★ 这是一个免费的测试 API，返回模拟的帖子数据\n\n")

        for (post in posts) {
            builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            builder.append("ID: ${post.id}\n")
            builder.append("用户ID: ${post.userId}\n")
            builder.append("标题: ${post.title}\n")
            builder.append("内容: ${post.body}\n\n")
        }

        tvResult.text = builder.toString()
    }
}