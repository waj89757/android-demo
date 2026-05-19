package com.example.demo01

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * ★ FragmentDemoActivity —— 演示 Fragment 的宿主 Activity
 *
 * 核心知识点：
 * 1. Activity 是 Fragment 的宿主（Fragment 不能独立存在）
 * 2. FragmentManager 负责 Fragment 的添加、替换、移除
 * 3. FragmentTransaction 支持 addToBackStack（返回键可以回退到上一个 Fragment）
 * 4. Activity 通过实现 Fragment 的接口，接收 Fragment 发来的消息
 */
class FragmentDemoActivity : AppCompatActivity(), ProfileFragment.OnProfileClickListener {

    private lateinit var btnTabHome: Button
    private lateinit var btnTabProfile: Button
    private lateinit var btnBackMain: Button
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_demo)

        btnTabHome = findViewById(R.id.btn_tab_home)
        btnTabProfile = findViewById(R.id.btn_tab_profile)
        btnBackMain = findViewById(R.id.btn_back_main)

        // ★ 初始加载 HomeFragment
        // 用 Bundle 传数据给 Fragment（衔接之前学的 Bundle）
        val homeFragment = HomeFragment.newInstance("欢迎来到首页！这是从 Activity 传来的数据")
        currentFragment = homeFragment
        switchFragment(homeFragment)

        // 切换到首页
        btnTabHome.setOnClickListener {
            val fragment = HomeFragment.newInstance("欢迎来到首页！")
            currentFragment = fragment
            switchFragment(fragment)
        }

        // 切换到个人主页
        btnTabProfile.setOnClickListener {
            val fragment = ProfileFragment.newInstance("王安杰", 10086, "VIP会员")
            currentFragment = fragment
            switchFragment(fragment)
        }

        // ★ 返回 MainActivity 主页
        // finish() 关闭当前 Activity，自动返回上一个页面
        btnBackMain.setOnClickListener {
            finish()
        }
    }

    /**
     * ★ 切换 Fragment 的核心方法
     *
     * FragmentTransaction 的工作流程：
     * 1. beginTransaction() —— 开启事务
     * 2. replace() —— 替换容器里的 Fragment（不是 add，是整体替换）
     * 3. addToBackStack() —— 把操作加入回退栈（按返回键可以回到上一个 Fragment）
     * 4. commit() —— 提交事务（异步执行，立刻返回）
     *
     * 类比后端数据库事务：
     *   beginTransaction → 一系列操作 → commit
     *   如果不 commit，所有操作都不会生效
     */
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)   // 替换容器内的 Fragment
            .addToBackStack(null)                          // 加入回退栈（支持返回键回退）
            .commit()                                      // 提交事务
    }

    // ★ 实现 ProfileFragment 的接口 —— 接收 Fragment 发来的消息
    // 这就是 Fragment → Activity 的通信方式
    override fun onMessageFromFragment(message: String) {
        Toast.makeText(this, "Activity 收到：$message", Toast.LENGTH_SHORT).show()
    }
}