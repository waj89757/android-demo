package com.example.demo

import com.example.network.Post
import com.example.network.RetrofitClient
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
 * ★ ViewModelDemoActivity —— 演示 ViewModel + LiveData 的用法
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  Activity 在 ViewModel 架构中的职责（只做两件事）：              │
 * │                                                              │
 * │  ① 观察 LiveData → 数据变了就更新UI                           │
 * │     viewModel.posts.observe(this) { posts -> 更新UI }        │
 * │                                                              │
 * │  ② 用户操作时调用 ViewModel 方法                              │
 * │     btn.setOnClickListener { viewModel.fetchPosts() }        │
 * │                                                              │
 * │  ★ Activity 不存数据、不发请求、不做业务逻辑                    │
 * │  ★ 所有数据和逻辑都在 ViewModel 里                             │
 * │  ★ 旋转屏幕 → Activity 重建 → 但 ViewModel 不重建             │
 * │     → ViewModel 里的数据还在 → observe 自动重新触发           │
 * │     → 界面瞬间恢复数据，不需要重新请求                          │
 * │                                                              │
 * │  ★ ViewModelProvider 的作用：                                 │
 * │     ViewModelProvider(this).get(PostViewModel::class.java)   │
 * │     它确保同一个 Activity 只创建一个 ViewModel 实例             │
 * │     Activity 重建时，ViewModelProvider 返回同一个 ViewModel   │
 * │     （不是新建一个，是复用旧的）                                 │
 * │                                                              │
 * │     类比后端：                                                 │
 * │     ViewModelProvider ≈ 单例工厂                              │
 * │     每次 get() → 如果已有实例就复用，没有才新建                  │
 * └──────────────────────────────────────────────────────────────┘
 */
class ViewModelDemoActivity : AppCompatActivity() {

    private lateinit var viewModel: PostViewModel
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvVmCount: TextView
    private lateinit var btnFetch: Button
    private lateinit var btnBackMain: Button

    // ★ ViewModel 创建次数计数（对比 Activity 重建次数）
    companion object {
        private var vmCreateCount = 0
        private var activityCreateCount = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewmodel_demo)

        activityCreateCount++   // ★ Activity 每次重建都递增
        tvStatus = findViewById(R.id.tv_status)
        tvResult = findViewById(R.id.tv_result)
        tvVmCount = findViewById(R.id.tv_vm_count)
        btnFetch = findViewById(R.id.btn_fetch_vm)
        btnBackMain = findViewById(R.id.btn_back_main)

        // ★ 关键第1步：获取 ViewModel 实例
        // ViewModelProvider(this) = 以当前 Activity 为 scope 获取 ViewModel
        // ★ 旋转屏幕时 Activity 重建，但 ViewModelProvider 返回的是同一个 ViewModel！
        // 因为 ViewModel 的生命周期比 Activity 长——它不随 Activity 重建而销毁
        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        // ★ 检查 ViewModel 是新建的还是复用的
        // 通过一个内部计数器来判断
        if (viewModel.isNewlyCreated()) {
            vmCreateCount++
        }
        tvVmCount.text = "ViewModel 创建次数：$vmCreateCount | Activity 重建次数：$activityCreateCount\n★ 旋转屏幕 → Activity次数递增，ViewModel次数不变！"

        // ★ 关键第2步：观察 LiveData —— 数据变化时自动更新UI
        // .observe(this, { ... }) = 注册一个观察者
        //   this = lifecycleOwner（当前 Activity），保证只在 Activity 前台时通知
        //   { posts -> ... } = 数据变化时执行的回调（在主线程）

        // 观察 posts LiveData
        viewModel.posts.observe(this) { posts ->
            // ★ 当 viewModel._posts.value 被设置时，这个回调自动触发
            // 结果就在这里！跟协程的 withContext 一样，数据自动送过来
            tvStatus.text = "✅ 数据已通过 LiveData 自动送达！获取到 ${posts.size} 条帖子"
            displayPosts(posts)
        }

        // 观察 loading LiveData
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                tvStatus.text = "⏳ 正在通过 ViewModel 发起网络请求..."
                btnFetch.isEnabled = false
            } else {
                btnFetch.isEnabled = true
            }
        }

        // 观察 error LiveData
        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                tvStatus.text = "❌ 请求失败：$errorMsg"
                Toast.makeText(this, "网络请求失败", Toast.LENGTH_SHORT).show()
            }
        }

        // ★ 关键第3步：用户操作时调用 ViewModel 方法
        // Activity 不自己发请求，而是让 ViewModel 去发
        btnFetch.setOnClickListener {
            viewModel.fetchPosts()  // ← 只调用 ViewModel 的方法，Activity 自己不发请求
        }

        btnBackMain.setOnClickListener { finish() }
    }

    private fun displayPosts(posts: List<Post>) {
        val builder = StringBuilder()
        builder.append("★ 数据来源：ViewModel → LiveData → 自动通知 Activity\n")
        builder.append("★ 旋转屏幕后数据还在（ViewModel 不随 Activity 重建而销毁）\n\n")

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