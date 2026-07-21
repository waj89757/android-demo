package com.example.demo01

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultReactActivityDelegate

/**
 * ★★★ KRNActivity：展示 React Native 页面的 Activity ★★★
 *
 * ReactActivity 是 RN 提供的基类 Activity。
 * 它内部创建 ReactRootView，加载 bundle，渲染你注册的组件。
 *
 * 工作流程：
 *   onCreate()
 *     → ReactActivityDelegate.loadApp("HelloKRN")
 *     → 创建 ReactRootView（RN 的根 View）
 *     → 通过 ReactNativeHost 获取 ReactInstanceManager
 *     → ReactInstanceManager 加载 bundle（assets/index.android.bundle）
 *     → 初始化 JS 运行时（Hermes）
 *     → 执行 bundle 里的 AppRegistry.registerComponent('HelloKRN', ...)
 *     → 渲染 HelloScreen 组件 → 显示到屏幕
 *
 * getMainComponentName() 返回的字符串 "HelloKRN" 必须和
 * JS 里 AppRegistry.registerComponent('HelloKRN', ...) 的第一个参数完全一致。
 *
 * 对照 KRN：
 *   快手 App 里打开 kleao://krn?bundleId=KleaoBriefProfile&componentName=KleaoBriefProfile
 *   KRN 容器 Activity 读 componentName → loadApp("KleaoBriefProfile")
 *   → 和这里的 getMainComponentName() 返回值作用完全相同
 */
class KRNActivity : ReactActivity() {

    // ★ 告诉 RN 要渲染哪个组件
    //   必须和 JS 的 AppRegistry.registerComponent 第一个参数一致
    override fun getMainComponentName(): String = "HelloKRN"

    // ★ ReactActivityDelegate：负责创建 ReactRootView、管理生命周期
    //   DefaultReactActivityDelegate 是 RN 提供的默认实现
    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, false)

    // ★ 可选：传 initialProps 给 RN 页面
    //   对照 KRN：URL params 经过 Activity 解析后作为 initialProps 传给 RN 组件
    //   这里暂时传空，后续可以从 Intent.extras 读取参数
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ReactActivity 的 onCreate 会自动调 delegate.loadApp()，不需要手动 setContentView
    }
}
