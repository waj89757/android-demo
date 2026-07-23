package com.example.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * ★ LifecycleFragment —— 可视化展示 Fragment 的完整生命周期
 *
 * Fragment 的完整生命周期（按触发顺序）：
 *
 * ┌──────────────────────────────────────────────────┐
 * │  onAttach        → Fragment 被添加到 Activity     │
 * │  onCreate        → Fragment 初始化（非视图相关）    │
 * │  onCreateView    → 创建并返回 Fragment 的布局      │
 * │  onViewCreated   → 视图创建完毕，可做初始化操作     │
 * │  ─── 可见状态开始 ───                              │
 * │  onStart         → Fragment 变为可见               │
 * │  onResume        → Fragment 可交互（焦点）          │
 * │  ─── 可交互状态 ───                                │
 * │  onPause         → Fragment 失去焦点               │
 * │  onStop          → Fragment 不可见                 │
 * │  ─── 不可见状态 ───                                │
 * │  onDestroyView   → Fragment 的视图被销毁           │
 * │  onDestroy       → Fragment 清理非视图资源          │
 * │  onDetach        → Fragment 从 Activity 移除       │
 * └──────────────────────────────────────────────────┘
 *
 * ★ 关键区别（对比 Activity 生命周期）：
 * 1. Fragment 多了 onCreateView / onDestroyView —— 视图的创建和销毁是独立的
 * 2. Fragment 的视图可以被销毁但 Fragment 对象还活着（onDestroyView ≠ onDestroy）
 * 3. 这就是为什么用 replace 时视图会销毁重建，但 show/hide 不会
 *
 * 后端类比：Fragment 生命周期就像一个微服务的心跳检测——
 *   onAttach = 注册到网关
 *   onCreateView = 启动服务实例
 *   onResume = 健康检查通过，开始接收请求
 *   onDestroyView = 停止服务实例（但服务配置还在）
 *   onDetach = 从网关注销
 */
class LifecycleFragment : Fragment() {

    companion object {
        private const val ARG_TAG_NAME = "tag_name"

        // ★ 全局创建计数器 —— 直观展示 replace 和 show/hide 的区别
        // replace 模式：每次切换都会重新创建 Fragment，计数器递增
        // show/hide 模式：只创建一次，切换时计数器不变
        private var globalCreateCount = 0

        // ★ 切换模式时重置计数器，方便对比观察
        fun resetCreateCount() {
            globalCreateCount = 0
        }

        fun newInstance(tagName: String): LifecycleFragment {
            val fragment = LifecycleFragment()
            val args = Bundle()
            args.putString(ARG_TAG_NAME, tagName)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var tvCreateCount: TextView
    private lateinit var tvLog: TextView
    private var logBuilder = StringBuilder()
    private var myTagName: String = ""

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        globalCreateCount++          // ★ 每次创建都计数
        myTagName = arguments?.getString(ARG_TAG_NAME, "未知") ?: "未知"
        appendLog("① onAttach → Fragment[$myTagName] 被添加到 Activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appendLog("② onCreate → Fragment[$myTagName] 初始化")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appendLog("③ onCreateView → Fragment[$myTagName] 创建视图")
        return inflater.inflate(R.layout.fragment_lifecycle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appendLog("④ onViewCreated → Fragment[$myTagName] 视图创建完毕")

        tvCreateCount = view.findViewById(R.id.tv_create_count)
        tvLog = view.findViewById(R.id.tv_lifecycle_log)

        // 显示创建次数
        tvCreateCount.text = "创建次数：$globalCreateCount（Replace模式会递增，Show/Hide不变）"
        // 刷新日志到屏幕
        tvLog.text = logBuilder.toString()
    }

    override fun onStart() {
        super.onStart()
        appendLog("⑤ onStart → Fragment[$myTagName] 变为可见")
    }

    override fun onResume() {
        super.onResume()
        appendLog("⑥ onResume → Fragment[$myTagName] 可交互（获得焦点）")
    }

    override fun onPause() {
        super.onPause()
        appendLog("⑦ onPause → Fragment[$myTagName] 失去焦点")
    }

    override fun onStop() {
        super.onStop()
        appendLog("⑧ onStop → Fragment[$myTagName] 不可见")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appendLog("⑨ onDestroyView → Fragment[$myTagName] 视图被销毁（★注意：Fragment对象还活着！）")
    }

    override fun onDestroy() {
        super.onDestroy()
        appendLog("⑩ onDestroy → Fragment[$myTagName] 清理资源")
    }

    override fun onDetach() {
        super.onDetach()
        appendLog("⑪ onDetach → Fragment[$myTagName] 从 Activity 移除")
    }

    // ★ 日志追加方法 —— 每个生命周期回调都会调用
    // 你能在屏幕上实时看到哪些回调被触发了
    private fun appendLog(msg: String) {
        logBuilder.append(msg).append("\n")
        // 如果视图还没创建，先缓存；视图创建后刷新到屏幕
        try {
            if (tvLog != null) {
                tvLog.text = logBuilder.toString()
            }
        } catch (e: Exception) {
            // 视图还没初始化，日志会在 onViewCreated 时统一刷新
        }
    }
}