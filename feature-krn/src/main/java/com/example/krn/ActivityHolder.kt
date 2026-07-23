package com.example.krn

import com.example.core.BannerHost

/**
 * ActivityHolder：持有宿主 Activity 的 BannerHost 引用
 *
 * 注意：这里持有的是 BannerHost 接口，不是 MainActivity 具体类
 * 原因：feature-krn 不能依赖 app（会循环），通过 core 里的接口解耦
 *
 * 生命周期：
 *   MainActivity.onCreate()  → ActivityHolder.host = this（实现了 BannerHost）
 *   MainActivity.onDestroy() → ActivityHolder.host = null
 */
object ActivityHolder {
    var host: BannerHost? = null
}
