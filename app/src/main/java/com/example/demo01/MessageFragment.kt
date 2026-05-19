package com.example.demo01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * ★ 消息页 Fragment —— ViewPager2 的第三个 Tab
 */
class MessageFragment : Fragment() {

    companion object {
        private const val ARG_TAB_INDEX = "tab_index"

        fun newInstance(tabIndex: Int): MessageFragment {
            val fragment = MessageFragment()
            val args = Bundle()
            args.putInt(ARG_TAB_INDEX, tabIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvInfo = view.findViewById<TextView>(R.id.tv_msg_info)
        val tabIndex = arguments?.getInt(ARG_TAB_INDEX, 2) ?: 2
        tvInfo.text = "这是第 $tabIndex 个Tab——消息页\n左右滑动可以切换到其他页面"

        // 模拟消息列表
        val rvList = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_msg_list)
        val adapter = MyTextAdapter(mutableListOf())
        rvList.adapter = adapter
        rvList.layoutManager = LinearLayoutManager(requireContext())

        val msgData = listOf("系统通知：欢迎注册", "用户A：你好！", "用户B：视频已发布", "官方：活动通知")
        adapter.refreshData(msgData)
    }
}