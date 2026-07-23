package com.example.core

import android.view.View

/**
 * BannerHost：宿主 Activity 向 feature-krn 暴露的能力接口
 *
 * 为什么需要这个接口？
 *
 * 依赖关系要求：
 *   app  → feature-krn（app 依赖 krn，krn 提供 RN 能力）
 *   app  → core（app 依赖 core，取接口定义）
 *   feature-krn → core（krn 依赖 core，只认接口不认具体类）
 *
 * 如果 feature-krn 直接 import MainActivity（在 app 里）：
 *   feature-krn → app → feature-krn  ← 循环依赖！编译报错
 *
 * 用接口打破循环：
 *   feature-krn 只知道「有个东西能 findView 和 runOnMain」
 *   MainActivity 在 app 里实现这个接口
 *   feature-krn 和 app 都依赖 core，互不依赖
 */
interface BannerHost {
    /** 通过资源 ID 找到宿主 Activity 里的 View */
    fun findViewByResId(id: Int): View?

    /** 把任务投递到宿主 Activity 的主线程执行 */
    fun runOnMainThread(action: () -> Unit)
}
