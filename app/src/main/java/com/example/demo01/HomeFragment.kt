package com.example.demo01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * ★ 首页 Fragment —— 演示 Fragment 的核心写法
 *
 * Fragment 的三个关键点：
 * 1. 继承 Fragment（不是 AppCompatActivity）
 * 2. onCreateView 返回布局（不是 setContentView）
 * 3. 用 arguments Bundle 接收外部传来的数据
 */
class HomeFragment : Fragment() {

    // ★ Fragment 传参的最佳实践：用 newInstance 工厂方法
    // 不允许外部直接调用构造器传参（系统重建 Fragment 时只会调用无参构造器）
    companion object {
        private const val ARG_WELCOME = "welcome_msg"

        // 外部调用 HomeFragment.newInstance("你好") 创建实例
        // Bundle 数据会被保存，系统重建时自动恢复
        fun newInstance(welcomeMsg: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_WELCOME, welcomeMsg)   // ★ 用 Bundle 传参（衔接你之前学的）
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var tvWelcome: TextView

    // ★ onCreateView —— Fragment 加载布局的核心方法
    // 参数说明：
    //   inflater: 用来加载 XML 布局（类似 Activity 的 setContentView）
    //   container: Fragment 要放入的父容器（由 Activity 提供）
    //   savedInstanceState: 状态恢复用的 Bundle（跟 Activity 一样）
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ★ inflate 布局：第三个参数 false 表示不立即添加到 container
        // 让 FragmentManager 来管理添加时机
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // ★ onViewCreated —— 视图创建后的初始化操作写这里
    // 比 onCreateView 更适合做 findViewById 等操作
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcome = view.findViewById(R.id.tv_home_welcome)

        // ★ 从 arguments 里取出传入的数据
        // arguments 就是之前 newInstance 里设置的 Bundle
        val welcomeMsg = arguments?.getString(ARG_WELCOME, "默认欢迎语")
        tvWelcome.text = "来自 Activity 的消息：$welcomeMsg"

        // 初始化列表（复用你之前学的 RecyclerView）
        val rvList = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_home_list)
        val adapter = MyTextAdapter(mutableListOf())
        rvList.adapter = adapter
        rvList.layoutManager = LinearLayoutManager(requireContext())

        val homeData = listOf("短视频推荐1", "短视频推荐2", "短视频推荐3", "热门挑战赛")
        adapter.refreshData(homeData)
    }
}