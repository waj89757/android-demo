package com.example.demo01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ★ WanasHomeViewModel —— 状态管理层
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  ViewModel 存在的核心价值                                      │
 * │  ──────────────────────────────────────────────────          │
 * │  1. 屏幕旋转不销毁（Activity/Fragment 重建，ViewModel 活着）    │
 * │  2. 数据与 UI 分离（Fragment 只管展示，不管数据从哪来）          │
 * │  3. 协程绑定生命周期（viewModelScope：ViewModel 销毁时协程自动取消）│
 * └──────────────────────────────────────────────────────────────┘
 *
 * ★ LiveData 的作用：
 *   Fragment 向 ViewModel "订阅"数据变化（observe）
 *   ViewModel 数据更新时，LiveData 自动通知 Fragment
 *   Fragment 不需要轮询，也不需要主动来拿 → 推送模式
 *
 * ★ 数据流向：
 *   Repository.fetchPage() → _items.value = ... → LiveData 通知 Fragment
 *   Fragment.observe → adapter.submitItems(newList)
 */
class WanasHomeViewModel : ViewModel() {

    private val repository = WanasRepository()

    // ─── 列表数据 ─────────────────────────────────────────────────
    // MutableLiveData：内部可修改（只有 ViewModel 内部能 .value = ...）
    // LiveData：对外只读（Fragment 只能 observe，不能修改）
    private val _items = MutableLiveData<List<WanasHomeItem>>(emptyList())
    val items: LiveData<List<WanasHomeItem>> = _items

    // ─── 加载状态 ─────────────────────────────────────────────────
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── 分页状态 ────────────────────────────────────────────────
    private var currentPage = 0
    var hasMoreData = true   // false 时不再触发加载，Fragment 可读取

    init {
        // ViewModel 创建时自动加载第一页
        loadNextPage()
    }

    /**
     * 加载下一页
     *
     * ★ 调用时机：
     *   1. init{} 里自动触发（首次加载）
     *   2. Fragment 检测到"滑到底部"时触发（加载更多）
     *
     * ★ 防重复请求：
     *   isLoading=true 时直接 return，避免用户快速滑动触发多次请求
     *   !hasMoreData 时直接 return，没有更多数据不再请求
     */
    fun loadNextPage() {
        if (_isLoading.value == true || !hasMoreData) return

        _isLoading.value = true

        // viewModelScope：绑定 ViewModel 生命周期的协程作用域
        // ViewModel 被销毁时，这里的协程自动取消，不会内存泄漏
        viewModelScope.launch {
            val nextPage = currentPage + 1

            // ★ suspend 调用：挂起协程 800ms（模拟网络），主线程不阻塞
            val newItems = repository.fetchPage(nextPage)

            if (newItems.isEmpty()) {
                // 没有更多数据了
                hasMoreData = false
            } else {
                currentPage = nextPage
                // 把新数据拼到现有列表后面（追加，不是替换）
                val current = _items.value.orEmpty().toMutableList()
                current.addAll(newItems)
                _items.value = current
            }

            _isLoading.value = false
        }
    }
}
