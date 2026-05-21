package com.example.demo01

/**
 * ★★★ MVP 的核心：Contract（契约接口）★★★
 *
 * Contract 是 MVP 架构的"说明书"，定义了这个页面：
 *   - View 能做什么（Activity 负责实现）
 *   - Presenter 能做什么（业务逻辑层实现）
 *
 * 你读公司 MVP 代码，永远先找 Contract 文件，
 * 一眼就能看懂这个页面的所有输入输出。
 *
 * ┌─────────────────────────────────────────────────────┐
 * │                   MVP 数据流向                        │
 * │                                                      │
 * │  用户点击按钮                                         │
 * │      ↓                                               │
 * │  Activity.btnLoad.click()                            │
 * │      ↓ 调用 Presenter 方法                            │
 * │  Presenter.loadPosts()                               │
 * │      ↓ 请求 Model 层                                  │
 * │  RetrofitClient.apiService.getPosts()                │
 * │      ↓ 数据回来后，主动调 View 方法                    │
 * │  view.showPosts(result)   ← Presenter 主动推          │
 * │      ↓                                               │
 * │  Activity 实现 showPosts()，刷新 UI                   │
 * │                                                      │
 * │  ★ 对比 MVVM：                                        │
 * │    MVVM 是 LiveData 被动通知（赋值自动触发回调）         │
 * │    MVP  是 Presenter 主动调用（view.showXxx()）        │
 * └─────────────────────────────────────────────────────┘
 */
interface PostMvpContract {

    /**
     * View 接口 —— 定义 Activity 必须实现的 UI 方法
     *
     * Presenter 通过这个接口操作 UI，
     * Presenter 持有的是这个接口，不是 Activity 本身。
     * 好处：Presenter 不依赖具体的 Activity 类，解耦。
     */
    interface View {
        /** 显示加载中（转圈圈） */
        fun showLoading()

        /** 隐藏加载中 */
        fun hideLoading()

        /** 展示帖子列表 */
        fun showPosts(posts: List<Post>)

        /** 展示错误信息 */
        fun showError(message: String)
    }

    /**
     * Presenter 接口 —— 定义业务操作方法
     *
     * Activity 通过这个接口调用业务逻辑，
     * Activity 持有的是这个接口，不是 PostMvpPresenter 本身。
     */
    interface Presenter {
        /** 加载帖子列表 */
        fun loadPosts()

        /**
         * ★ 必须在 Activity.onDestroy() 里调用！
         * 解绑 View 引用，防止内存泄漏。
         * 这是 MVP 最容易忘记的地方，也是最常见的 Bug。
         */
        fun onDestroy()
    }
}
