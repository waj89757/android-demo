package com.example.demo01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WanasHomeFragment : Fragment() {

    private val viewModel: WanasHomeViewModel by activityViewModels()

    private var recyclerView: RecyclerView? = null
    private var adapter: WanasHomeAdapter? = null

    // 记录上次 observe 时 items 的大小，用于判断是否有新数据追加
    private var lastObservedSize = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wanas_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterInstance = WanasHomeAdapter()
        adapter = adapterInstance

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView = view.findViewById<RecyclerView>(R.id.rv_wanas_home).also { rv ->
            rv.layoutManager = layoutManager
            rv.adapter = adapterInstance

            // ★ 滚动监听：检测"是否滑到底部"触发加载下一页
            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // 只在向下滑时触发（dy > 0 = 内容向上走 = 手指向上）
                    if (dy <= 0) return
                    checkAndLoadMore(layoutManager, adapterInstance)
                }
            })
        }

        // ★ 观察 items LiveData：每次 ViewModel 有新数据时追加到列表
        viewModel.items.observe(viewLifecycleOwner) { allItems ->
            if (allItems.size > lastObservedSize) {
                val newPage = allItems.subList(lastObservedSize, allItems.size)
                // hasMoreData 来自 ViewModel，表示是否还有下一页
                adapterInstance.appendPage(newPage, viewModel.hasMoreData)
                lastObservedSize = allItems.size

                // ★ 数据加载完成后，检查一次：如果第一页就已经撑满屏幕底部
                //   需要触发第二页加载（不依赖滑动，因为用户可能还没滑）
                recyclerView?.post {
                    val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return@post
                    val adapter = recyclerView?.adapter ?: return@post
                    checkAndLoadMore(lm, adapter as WanasHomeAdapter)
                }
            }
        }
    }

    /**
     * ★ 核心检查：最后可见 item 是否接近列表末尾
     *
     * 触发时机：
     *   1. 用户向下滑动时（onScrolled）
     *   2. 每次新一页数据加载完后（post 延迟检查）
     *      → 解决"第一页不够撑满屏幕时，用户不需要滑动也能触发下一页"的场景
     *      → 同时解决"LoadingItem 出现在屏幕内但用户不滑动"的场景
     */
    private fun checkAndLoadMore(layoutManager: LinearLayoutManager, adapterInstance: WanasHomeAdapter) {
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        val total = adapterInstance.itemCount
        // 滑到倒数第3条时触发（提前请求，让加载对用户透明）
        if (total > 0 && lastVisible >= total - 3) {
            viewModel.loadNextPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null
    }
}
