package com.example.demo01

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ★ FragmentStateAdapter —— ViewPager2 的灵魂组件
 *
 * ViewPager2 + Fragment 的工作原理：
 *
 * ┌──────────────────────────────────────────────────────────┐
 * │  ViewPager2 是什么？                                       │
 * │  ─────────────────────────────────                        │
 * │  本质是一个增强版 RecyclerView，把每个 Fragment 当成列表条目 │
 * │  用户左右滑动时，ViewPager2 自动创建/销毁/切换 Fragment      │
 * │                                                           │
 * │  FragmentStateAdapter 是什么？                              │
 * │  ─────────────────────────────────                        │
 * │  类比后端：这就是一个"路由控制器"                              │
 * │  前端类比：相当于 React Router，根据 URL 返回对应页面组件     │
 * │                                                           │
 * │  它的职责：                                                 │
 * │  1. getItemCount() → 有多少个页面（Tab数量）                 │
 * │  2. createFragment(position) → 第position个位置放哪个Fragment │
 * │                                                           │
 * │  ViewPager2 会自动调用这两个方法：                            │
 * │  - 滑到第0页 → 调 createFragment(0) → 返回首页Fragment      │
 * │  - 滑到第1页 → 调 createFragment(1) → 返回发现Fragment      │
 * │  - 滑到第2页 → 调 createFragment(2) → 返回消息Fragment      │
 * │                                                           │
 * │  ★ 重要：ViewPager2 内置了 Fragment 的懒加载机制             │
 * │  默认只创建当前页 + 左右各1页（offscreenPageLimit=1）         │
 * │  所以3个Tab只创建2~3个Fragment，不会全部创建浪费内存           │
 * └──────────────────────────────────────────────────────────┘
 *
 * 构造器参数说明：
 * - FragmentActivity：传 Activity（ViewPager2 在 Activity 里用）
 * - Fragment：传 Fragment（ViewPager2 在 Fragment 里嵌套用）
 *
 * 这里我们传 FragmentActivity，因为 ViewPager2 在 ViewPagerDemoActivity 里
 */
class ViewPagerTabAdapter(
    fragmentActivity: FragmentActivity,
    private val tabTitles: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    // ★ 返回 Tab 数量 —— ViewPager2 知道要准备多少个页面
    override fun getItemCount(): Int {
        return tabTitles.size
    }

    // ★ 返回指定位置的 Fragment —— ViewPager2 滑到某页时自动调用
    // position 就像后端路由的参数，决定返回哪个"页面组件"
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance("ViewPager2首页：左右滑动试试！")
            1 -> DiscoverFragment.newInstance(position)
            2 -> MessageFragment.newInstance(position)
            else -> HomeFragment.newInstance("默认页")
        }
    }
}