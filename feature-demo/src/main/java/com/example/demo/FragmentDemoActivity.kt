package com.example.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * ★ FragmentDemoActivity —— 演示 Fragment 切换的两种模式
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Replace 模式（你之前用的）                                    │
 * │  ─────────────────────────────────                           │
 * │  原理：replace = 移除旧Fragment + 添加新Fragment              │
 * │  效果：每次切换都会触发旧Fragment的完整销毁生命周期               │
 * │        onDestroyView → onDestroy → onDetach                   │
 * │        新Fragment触发完整创建生命周期                            │
 * │        onAttach → onCreate → onCreateView → onViewCreated     │
 * │  优点：内存占用低（不用的Fragment被销毁释放）                    │
 * │  缺点：每次切换都重建，有延迟，状态丢失                          │
 * │                                                               │
 * │  ★ 适合场景：页面之间没有频繁切换                               │
 * │                                                               │
 * │  Show/Hide 模式（推荐用于Tab切换）                              │
 * │  ─────────────────────────────────                           │
 * │  原理：add 一次性添加所有Fragment，切换时只 show/hide           │
 * │  效果：Fragment 只创建一次，切换时只触发                        │
 * │        onResume / onPause（不会销毁视图）                      │
 * │  优点：切换快，状态保留，用户体验好                              │
 * │  缺点：所有Fragment都在内存里，内存占用稍高                      │
 * │                                                               │
 * │  ★ 适合场景：底部Tab导航、频繁切换的页面                        │
 * │  （短视频App的首页/发现/消息/个人主页 就是这个模式）              │
 * └─────────────────────────────────────────────────────────────┘
 */
class FragmentDemoActivity : AppCompatActivity(), ProfileFragment.OnProfileClickListener {

    private lateinit var btnTabHome: Button
    private lateinit var btnTabProfile: Button
    private lateinit var btnTabLifecycle: Button
    private lateinit var btnBackMain: Button
    private lateinit var btnModeReplace: Button
    private lateinit var btnModeShowHide: Button
    private lateinit var tvCurrentMode: TextView

    // ★ 切换模式标记
    private var isReplaceMode = true

    // ★ Show/Hide 模式下，预先创建的 Fragment 引用
    // 这些 Fragment 只创建一次，切换时只做 show/hide
    private var homeFragment: HomeFragment? = null
    private var profileFragment: ProfileFragment? = null
    private var lifecycleFragment: LifecycleFragment? = null
    private var currentShowFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_demo)

        btnTabHome = findViewById(R.id.btn_tab_home)
        btnTabProfile = findViewById(R.id.btn_tab_profile)
        btnTabLifecycle = findViewById(R.id.btn_tab_lifecycle)
        btnBackMain = findViewById(R.id.btn_back_main)
        btnModeReplace = findViewById(R.id.btn_mode_replace)
        btnModeShowHide = findViewById(R.id.btn_mode_show_hide)
        tvCurrentMode = findViewById(R.id.tv_current_mode)

        // ★ 初始加载首页
        switchToHome()

        // ──── Tab 按钮 ────
        btnTabHome.setOnClickListener { switchToHome() }
        btnTabProfile.setOnClickListener { switchToProfile() }
        btnTabLifecycle.setOnClickListener { switchToLifecycle() }
        btnBackMain.setOnClickListener { finish() }

        // ──── 模式切换按钮 ────
        btnModeReplace.setOnClickListener {
            isReplaceMode = true
            tvCurrentMode.text = "当前：Replace"
            tvCurrentMode.setTextColor(android.graphics.Color.parseColor("#F44336"))
            // 切换模式时重置 Show/Hide 的缓存 Fragment
            // 因为之前 show/hide 的 Fragment 还在容器里，需要清掉
            clearAllFragments()
            Toast.makeText(this, "切换到 Replace 模式：每次切换销毁重建", Toast.LENGTH_SHORT).show()
            switchToHome()
        }

        btnModeShowHide.setOnClickListener {
            isReplaceMode = false
            tvCurrentMode.text = "当前：Show/Hide"
            tvCurrentMode.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            // 切换模式时清掉容器里的 Fragment，重新用 add 添加
            clearAllFragments()
            // 重置生命周期计数器，让观察更清晰
            LifecycleFragment.resetCreateCount()
            Toast.makeText(this, "切换到 Show/Hide 模式：只创建一次，切换不销毁", Toast.LENGTH_SHORT).show()
            switchToHome()
        }
    }

    // ──── Tab 切换方法 ────

    private fun switchToHome() {
        if (isReplaceMode) {
            // ★ Replace 模式：每次创建新 Fragment，替换容器里的旧 Fragment
            val fragment = HomeFragment.newInstance("欢迎来到首页！（Replace模式）")
            replaceFragment(fragment)
        } else {
            // ★ Show/Hide 模式：Fragment 只创建一次，之后只做 show/hide
            if (homeFragment == null) {
                homeFragment = HomeFragment.newInstance("欢迎来到首页！（Show/Hide模式）")
                addFragment(homeFragment!!)
            }
            showFragment(homeFragment!!)
        }
    }

    private fun switchToProfile() {
        if (isReplaceMode) {
            val fragment = ProfileFragment.newInstance("王安杰", 10086, "VIP会员")
            replaceFragment(fragment)
        } else {
            if (profileFragment == null) {
                profileFragment = ProfileFragment.newInstance("王安杰", 10086, "VIP会员")
                addFragment(profileFragment!!)
            }
            showFragment(profileFragment!!)
        }
    }

    private fun switchToLifecycle() {
        if (isReplaceMode) {
            val fragment = LifecycleFragment.newInstance("Lifecycle观察")
            replaceFragment(fragment)
        } else {
            if (lifecycleFragment == null) {
                lifecycleFragment = LifecycleFragment.newInstance("Lifecycle观察")
                addFragment(lifecycleFragment!!)
            }
            showFragment(lifecycleFragment!!)
        }
    }

    // ──── ★ Replace 模式的核心方法 ────

    /**
     * Replace 模式：移除旧Fragment，添加新Fragment
     *
     * 触发的生命周期：
     *   旧Fragment: onPause → onStop → onDestroyView → onDestroy → onDetach
     *   新Fragment: onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
     *
     * 类比：就像每次HTTP请求都重新启动一个服务实例
     * 旧实例完全关闭，新实例重新启动——代价高但干净
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ──── ★ Show/Hide 模式的核心方法 ────

    /**
     * Show/Hide 模式第一步：add 添加 Fragment（只执行一次）
     *
     * 触发的生命周期：
     *   onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
     *
     * 之后切换 Tab 时不会再次触发这些——只触发 onResume / onPause
     *
     * 类比：就像服务启动后一直运行，只是有时候不接收请求（hide）
     * 服务实例始终存在，只是暂时不对外服务
     */
    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Show/Hide 模式第二步：切换时 show 目标 Fragment，hide 当前 Fragment
     *
     * 触发的生命周期：
     *   当前Fragment: onPause → onStop（★不会触发 onDestroyView！）
     *   目标Fragment: onStart → onResume（★不会触发 onCreateView！）
     *
     * 这就是 Show/Hide 模式的核心优势——视图不被销毁，状态被保留
     *
     * 类比：就像把一个服务从"待命"状态切换到"活跃"状态
     * 服务一直运行着，只是激活/待命的切换
     */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                // hide 当前显示的 Fragment
                if (currentShowFragment != null && currentShowFragment != fragment) {
                    hide(currentShowFragment!!)
                }
                // show 目标 Fragment
                show(fragment)
            }
            .commit()
        currentShowFragment = fragment
    }

    // 清除容器里所有 Fragment（切换模式时使用）
    private fun clearAllFragments() {
        homeFragment = null
        profileFragment = null
        lifecycleFragment = null
        currentShowFragment = null

        // 移除容器里所有 Fragment
        val transaction = supportFragmentManager.beginTransaction()
        for (frag in supportFragmentManager.fragments) {
            transaction.remove(frag)
        }
        transaction.commit()

        // 清空回退栈
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // ★ 实现 ProfileFragment 的接口
    override fun onMessageFromFragment(message: String) {
        Toast.makeText(this, "Activity 收到：$message", Toast.LENGTH_SHORT).show()
    }
}