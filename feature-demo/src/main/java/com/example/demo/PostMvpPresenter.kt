package com.example.demo

import com.example.network.Post
import com.example.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ★★★ MVP 的核心：Presenter（业务逻辑层）★★★
 *
 * Presenter 的职责：
 *   1. 持有 View 接口引用（通过接口操作 UI，不直接依赖 Activity）
 *   2. 调用 Model 层（RetrofitClient）获取数据
 *   3. 处理业务逻辑，把结果"主动推给"View
 *
 * ★ 和 MVVM 的 ViewModel 最大区别：
 *
 *   ViewModel（MVVM）：
 *     _posts.value = result   ← 赋值，LiveData 自动通知 Activity
 *     ViewModel 不知道 Activity 的存在
 *
 *   Presenter（MVP）：
 *     view?.showPosts(result) ← 直接调 Activity 的方法
 *     Presenter 持有 View 引用，知道 Activity 的存在（通过接口）
 */
class PostMvpPresenter(
    // ★ 持有的是 View 接口，不是 MvpDemoActivity 本身
    // 用 var 而不是 val，因为 onDestroy 时要置 null
    private var view: PostMvpContract.View?
) : PostMvpContract.Presenter {

    // 协程 Job，用来在 onDestroy 时取消正在进行的网络请求
    private var job: Job? = null

    // Presenter 自己创建协程作用域（对比 MVVM：ViewModel 有 viewModelScope）
    // Dispatchers.Main 保证回调在主线程执行（可以直接更新 UI）
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * ★ 业务方法：加载帖子列表
     *
     * 对比 MVVM 的 PostViewModel.fetchPosts()，逻辑几乎一样，
     * 区别只在最后一步：
     *   MVVM：_posts.value = result      (赋值给 LiveData)
     *   MVP ：view?.showPosts(result)    (直接调 View 方法)
     */
    override fun loadPosts() {
        // 1. 通知 View 显示加载中
        view?.showLoading()

        job = scope.launch {
            try {
                // 2. 切到 IO 线程做网络请求（和 MVVM 完全一样）
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getPosts(5)
                }

                // 3. ★ MVP 的关键：直接调 View 的方法通知 UI
                //    此时已经回到主线程（因为 scope 用 Dispatchers.Main）
                view?.hideLoading()
                view?.showPosts(result)

                // ★ 对比 MVVM：
                // _posts.value = result    ← MVVM 写法（赋值，LiveData 自动推送）
                // view?.showPosts(result)  ← MVP  写法（主动调 View 方法）

            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "网络请求失败")
            }
        }
    }

    /**
     * ★★★ 最重要的方法：onDestroy()
     *
     * Activity 销毁时必须调用，否则：
     *   - Presenter 还持有旧 Activity 的引用
     *   - 旧 Activity 无法被 GC 回收 → 内存泄漏
     *   - 如果网络请求还在进行，回调回来后调用已销毁 Activity 的方法 → 崩溃
     *
     * ★ 这是 MVP 相比 MVVM 最麻烦的地方
     *   MVVM 的 ViewModel 由框架自动管理生命周期，完全不需要手动处理
     */
    override fun onDestroy() {
        job?.cancel()    // 取消正在进行的网络请求
        view = null      // ★ 解绑 View，防止内存泄漏
    }
}
