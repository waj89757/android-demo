package com.example.demo01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ★ PostViewModel —— 管理帖子数据的 ViewModel
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  ViewModel 是什么？                                           │
 * │  ─────────────────────────────────                            │
 * │  ViewModel = 专门存数据的对象，跟 Activity 的生命周期分离       │
 * │  Activity 销毁重建时（如屏幕旋转），ViewModel 不销毁            │
 * │  数据在 ViewModel 里，不会随 Activity 一起丢失                 │
 * │                                                               │
 * │  后端类比：                                                    │
 * │    Activity = HTTP 请求（短生命周期，请求结束就没了）            │
 * │    ViewModel = Redis 缓存（跨请求持久存在）                     │
 * │    LiveData = WebSocket 推送（数据变了自动通知订阅者）           │
 * │                                                               │
 * │  ★ MVVM 架构（现代 Android 标准架构）                          │
 * │  ─────────────────────────────────                            │
 * │    Model      → 数据层（Retrofit API + 数据类）                │
 * │    ViewModel  → 逻辑层（管理数据，发请求，存状态）               │
 * │    View       → 展示层（Activity/Fragment，只负责渲染UI）       │
 * │                                                               │
 * │    类比后端 MVC：                                              │
 * │    Model = 数据库/缓存                                        │
 * │    ViewModel = Service 层（业务逻辑）                           │
 * │    View = Controller + 前端模板（展示+交互）                    │
 * │                                                               │
 * │    ★ 关键原则：Activity 不直接存数据，不直接发请求               │
 * │    Activity 只做两件事：                                       │
 * │    1. 观察 ViewModel 的 LiveData，数据变了就更新UI              │
 * │    2. 用户操作时调用 ViewModel 的方法                           │
 * │    所有业务逻辑和数据都在 ViewModel 里                          │
 * └─────────────────────────────────────────────────────────────┘
 *
 * LiveData 是什么？
 * ─────────────────────────────────
 * LiveData = 可观察的数据容器，数据变化时自动通知观察者
 *
 * ★ 两个变体：
 *   MutableLiveData —— 可修改的版本（ViewModel 内部用）
 *   LiveData        —— 只读版本（暴露给外部用）
 *
 * ★ 为什么暴露只读的 LiveData？
 *   防止外部（Activity）直接修改数据，只有 ViewModel 能改
 *   类比后端：private setter + public getter —— 封装原则
 *
 * ★ LiveData 的生命周期感知：
 *   只有当 Activity 在前台时才会收到通知
 *   Activity 在后台时，数据变了但不会收到通知（避免崩溃）
 *   Activity 回到前台时，自动拿到最新数据
 */
class PostViewModel : ViewModel() {

    // ★ 用于判断 ViewModel 是否是新建的（对比 Activity 重建次数）
    private var newlyCreated = true

    fun isNewlyCreated(): Boolean {
        if (newlyCreated) {
            newlyCreated = false
            return true
        }
        return false
    }

    // ★ MutableLiveData —— ViewModel 内部可以修改数据
    // 私有的，外部不能直接改
    private val _posts = MutableLiveData<List<Post>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _error = MutableLiveData<String?>()

    // ★ LiveData —— 只读版本，暴露给 Activity 观察
    // 外部只能读，不能改 —— 类似 private setter + public getter
    val posts: LiveData<List<Post>> = _posts
    val loading: LiveData<Boolean> = _loading
    val error: LiveData<String?> = _error

    // ★ viewModelScope —— ViewModel 专用的协程作用域
    // 和 lifecycleScope 类似，但 ViewModel 销毁时自动取消所有协程
    // 不会因为 Activity 销毁而取消（因为 ViewModel 活得更久）
    fun fetchPosts() {
        _loading.value = true      // ★ 设置 LiveData 的值 = 通知所有观察者
        _error.value = null

        // ★ viewModelScope.launch —— 在 ViewModel 的协程作用域里启动
        // 协程的生命周期跟 ViewModel 绑定，不是跟 Activity 绑定
        // Activity 重建了，ViewModel 还在，协程还在跑
        viewModelScope.launch {
            try {
                // 切到 IO 子线程发网络请求
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getPosts(5)
                }

                // ★ .value = 给 LiveData 设值，所有观察者（Activity）自动收到通知
                _posts.value = result
                _loading.value = false

            } catch (e: Exception) {
                _error.value = e.message
                _loading.value = false
            }
        }
    }

    // ★ onCleared —— ViewModel 销毁时调用（Activity 真正被关闭，不是旋转重建）
    // 旋转屏幕时不会调用 onCleared（ViewModel 还活着）
    // 只有当 Activity 真正 finish() 了，ViewModel 才会销毁
    override fun onCleared() {
        super.onCleared()
        // 可以在这里清理资源（如关闭数据库连接等）
    }
}