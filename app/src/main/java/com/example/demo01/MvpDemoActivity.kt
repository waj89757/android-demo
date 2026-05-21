package com.example.demo01

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * ★★★ MVP 架构 Demo —— Activity 扮演 View 角色 ★★★
 *
 * Activity 在 MVP 里做三件事：
 *   1. 实现 Contract.View 接口（被动接收 Presenter 的通知并更新 UI）
 *   2. 持有 Presenter 引用（主动触发业务操作）
 *   3. 在 onDestroy() 里调 presenter.onDestroy() 解绑
 *
 * ★ 和 MVVM 的 ViewModelDemoActivity 对比：
 *
 *   MVVM Activity：
 *     viewModel.posts.observe(this) { posts -> adapter.refresh(posts) }
 *     ← 注册观察者，等 LiveData 推过来，被动等待
 *
 *   MVP Activity：
 *     implements PostMvpContract.View
 *     override fun showPosts(posts) { adapter.refresh(posts) }
 *     ← 实现接口方法，等 Presenter 主动调用，被动等待
 *
 *   两者的"被动等待"方式不同：
 *     MVVM → 变量赋值触发（Observer 模式）
 *     MVP  → 函数调用触发（接口回调模式）
 */
class MvpDemoActivity : AppCompatActivity(),
    PostMvpContract.View {  // ★ 实现 View 接口

    // ★ 持有 Presenter 接口（不是 PostMvpPresenter 本身）
    private lateinit var presenter: PostMvpContract.Presenter

    private lateinit var btnLoad: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvPosts: RecyclerView
    private lateinit var adapter: MyTextAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvp_demo)

        // ★ 创建 Presenter，把自己（this）作为 View 传进去
        // this 实现了 PostMvpContract.View 接口，所以可以传
        presenter = PostMvpPresenter(this)

        initView()

        btnLoad.setOnClickListener {
            // ★ Activity 只管调 Presenter 方法，不写任何业务逻辑
            // 对比 MVVM：viewModel.fetchPosts()
            presenter.loadPosts()
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun initView() {
        btnLoad = findViewById(R.id.btn_load)
        tvStatus = findViewById(R.id.tv_status)
        progressBar = findViewById(R.id.progress_bar)
        rvPosts = findViewById(R.id.rv_posts)

        adapter = MyTextAdapter(mutableListOf())
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)
    }

    // ══════════════════════════════════════════════════════
    // ★★★ 以下是 PostMvpContract.View 接口的实现 ★★★
    //
    // 这些方法不是你主动调用的！
    // 是 Presenter 在合适时机调用的：
    //   presenter → view?.showLoading()    → 这里执行
    //   presenter → view?.showPosts(...)   → 这里执行
    //   presenter → view?.showError(...)   → 这里执行
    // ══════════════════════════════════════════════════════

    override fun showLoading() {
        // Presenter 调用我 → 我显示转圈圈
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "加载中..."
        btnLoad.isEnabled = false
    }

    override fun hideLoading() {
        // Presenter 调用我 → 我隐藏转圈圈
        progressBar.visibility = View.GONE
        btnLoad.isEnabled = true
    }

    override fun showPosts(posts: List<Post>) {
        // ★ Presenter 主动把数据推给我，我负责显示
        // 对比 MVVM：这段逻辑在 observe() 的 lambda 里
        tvStatus.text = "加载成功，共 ${posts.size} 条"
        val displayList = posts.map { "ID:${it.id} | ${it.title}" }
        adapter.refreshData(displayList)
    }

    override fun showError(message: String) {
        // Presenter 调用我 → 我显示错误
        tvStatus.text = "加载失败：$message"
    }

    // ★★★ 最关键：onDestroy 必须解绑 ★★★
    override fun onDestroy() {
        // 如果漏写这行：
        //   - Presenter 里的 job 协程还在跑
        //   - Presenter 还持有这个 Activity 的引用
        //   - Activity 无法被 GC 回收 → 内存泄漏
        //   - 协程回来后调 view?.showPosts()，但 Activity 已经销毁 → 可能崩溃
        presenter.onDestroy()
        super.onDestroy()
    }
}
