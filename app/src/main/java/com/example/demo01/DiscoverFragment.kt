package com.example.demo01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * ★ 发现页 Fragment —— ViewPager2 的第二个 Tab
 */
class DiscoverFragment : Fragment() {

    companion object {
        private const val ARG_TAB_INDEX = "tab_index"

        fun newInstance(tabIndex: Int): DiscoverFragment {
            val fragment = DiscoverFragment()
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
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvInfo = view.findViewById<TextView>(R.id.tv_discover_info)
        val tabIndex = arguments?.getInt(ARG_TAB_INDEX, 1) ?: 1
        tvInfo.text = "这是第 $tabIndex 个Tab——发现页\n左右滑动可以切换到其他页面"

        // 模拟热门内容列表
        val rvList = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_discover_list)
        val adapter = MyTextAdapter(mutableListOf())
        rvList.adapter = adapter
        rvList.layoutManager = LinearLayoutManager(requireContext())

        val discoverData = listOf("热门挑战赛 #1", "热门挑战赛 #2", "达人推荐", "同城热门")
        adapter.refreshData(discoverData)
    }
}