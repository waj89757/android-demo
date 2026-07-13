package com.example.demo01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Wanas 首页 Activity —— Fragment + BottomNavigation 的完整示范
 *
 * ★ 核心架构：Activity 只是一个"容器壳子"
 *
 *   Activity 的职责：
 *   1. 持有 BottomNavigationView（底部导航栏）
 *   2. 管理 Fragment 的切换（show/hide 模式）
 *   3. 不包含任何业务 UI，所有内容都在 Fragment 里
 *
 *   Fragment 的职责：
 *   1. 展示具体内容（列表、按钮、输入框等）
 *   2. 包含自己的布局和逻辑
 *   3. 生命周期受 Activity 管理
 *
 * ★ 为什么用 show/hide 而不是 replace：
 *   - replace：每次切 Tab 都销毁旧 Fragment，重建新 Fragment
 *              列表滚动位置丢失，每次都要重新请求数据
 *   - show/hide：Fragment 只创建一次，切换时只改可见性
 *               列表状态保留，切回来不重新加载
 *   → Wanas/微信/抖音/快手 底部 Tab 全是 show/hide
 */
class WanasActivity : AppCompatActivity() {

    // ★ 四个 Fragment 在 Activity 初始化时就创建好，之后只做 show/hide
    // 用 lazy 延迟到第一次用时才初始化
    private val homeFragment    by lazy { WanasHomeFragment() }
    private val roomsFragment   by lazy { WanasPlaceholderFragment.newInstance("🏠 Rooms\n（即将上线）") }
    private val inboxFragment   by lazy { WanasPlaceholderFragment.newInstance("📬 Inbox\n（即将上线）") }
    private val profileFragment by lazy { WanasPlaceholderFragment.newInstance("👤 Profile\n（即将上线）") }

    // 当前显示的 Fragment，用于 hide
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wanas)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // ─── 初始显示首页 ─────────────────────────────────
        showFragment(homeFragment)

        // ─── 底部导航点击事件 ──────────────────────────────
        // setOnItemSelectedListener：点击每个 Tab 时触发
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home    -> { showFragment(homeFragment);    true }
                R.id.nav_rooms   -> { showFragment(roomsFragment);   true }
                R.id.nav_inbox   -> { showFragment(inboxFragment);   true }
                R.id.nav_profile -> { showFragment(profileFragment); true }
                else             -> false
            }
        }
    }

    /**
     * ★ show/hide 切换的核心方法
     *
     * 第一次切换到某个 Fragment：
     *   - supportFragmentManager.beginTransaction().add(container, fragment)
     *   - 触发完整生命周期：onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
     *
     * 后续切换：
     *   - hide 当前 Fragment：触发 onPause → onStop（不触发 onDestroyView！视图保留）
     *   - show 目标 Fragment：触发 onStart → onResume（不触发 onCreateView！直接恢复）
     *
     * ★ beginTransaction / commit 是什么：
     *   - FragmentManager 管理 Fragment 的工具
     *   - beginTransaction() 开启一次"操作事务"（可以包含多个 add/remove/show/hide）
     *   - commit() 提交这次事务，让改动生效
     *   - 类比数据库事务：begin → 一批操作 → commit
     */
    private fun showFragment(target: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        if (!target.isAdded) {
            // ★ 第一次：还没被 add 进容器，执行 add
            transaction.add(R.id.wanas_fragment_container, target)
        }

        // hide 当前显示的（如果有）
        currentFragment?.let { current ->
            if (current != target) transaction.hide(current)
        }

        // show 目标
        transaction.show(target)
        transaction.commit()

        currentFragment = target
    }
}
