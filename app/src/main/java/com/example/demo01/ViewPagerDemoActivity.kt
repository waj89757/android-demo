package com.example.demo01

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * ★ ViewPagerDemoActivity —— 演示 ViewPager2 + Fragment + TabLayout
 *
 * 这就是短视频 App 的标准导航结构：
 * 抖音/快手 底部 4 个 Tab（首页/发现/消息/个人）
 * 左右滑动切换页面，点击 Tab 也切换
 *
 * ┌────────────────────────────────────────────────────┐
 * │  ViewPager2 + TabLayout 联动原理                     │
 * │  ─────────────────────────────────                  │
 * │  1. TabLayout 显示标签（首页/发现/消息）               │
 * │  2. ViewPager2 负责滑动的 Fragment 切换              │
 * │  3. TabLayoutMediator 把两者绑定在一起               │
 * │     - 滑动 ViewPager2 → TabLayout 标签自动跟随       │
 * │     - 点击 TabLayout → ViewPager2 跳到对应页面       │
 * │  4. FragmentStateAdapter 提供 Fragment               │
 * │     - ViewPager2 滑到哪个位置 → Adapter 创建对应Fragment│
 * │                                                     │
 * │  ★ TabLayoutMediator 是关键的"粘合剂"                │
 * │    不用它的话，Tab和ViewPager各走各的，不联动           │
 * └────────────────────────────────────────────────────┘
 */
class ViewPagerDemoActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnBackMain: Button

    // ★ Tab 标题列表
    private val tabTitles = listOf("首页", "发现", "消息")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager_demo)

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        btnBackMain = findViewById(R.id.btn_back_main)

        // ★ 第1步：创建 Adapter 并设置给 ViewPager2
        // Adapter 告诉 ViewPager2：3个页面，每个位置放哪个 Fragment
        val adapter = ViewPagerTabAdapter(this, tabTitles)
        viewPager.adapter = adapter

        // ★ 第2步：用 TabLayoutMediator 把 TabLayout 和 ViewPager2 绑定
        // 这是三者联动的关键！
        //
        // TabLayoutMediator 做了什么：
        // - 给 TabLayout 添加标签文字（来自 tabTitles）
        // - 滑动 ViewPager2 时，自动切换 TabLayout 的选中标签
        // - 点击 TabLayout 标签时，自动跳转到 ViewPager2 对应页面
        //
        // 参数说明：
        //   tabLayout：标签栏
        //   viewPager：滑动容器
        //   autoRefresh：true 表示 Adapter 数据变化时自动更新标签
        //   { tab, position -> }：给每个标签设置文字
        //   .attach()：★ 必须调用！不 attach 就不会联动
        val mediator = TabLayoutMediator(tabLayout, viewPager, true) { tab, position ->
            tab.text = tabTitles[position]
        }
        mediator.attach()   // ★ attach 是关键！没它就不联动

        // ★ 第3步（可选）：设置预加载页数
        // offscreenPageLimit = 1 表示：当前页 + 左右各1页 = 最多3个Fragment在内存中
        // 这就是 ViewPager2 的懒加载——不会一次性创建所有Fragment
        viewPager.offscreenPageLimit = 1

        // 返回主页
        btnBackMain.setOnClickListener { finish() }
    }
}