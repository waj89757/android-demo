package com.example.demo

/**
 * 首页列表的数据模型 —— 多类型 RecyclerView 的基础
 *
 * 核心设计：用 sealed class 定义"只能是这几种类型之一"的数据
 *
 * sealed class 比普通 class 的优势：
 *   when(item) { is UserItem -> ... is BannerItem -> ... }
 *   编译器会强制你处理所有子类，不会漏掉任何类型
 *
 * 这直接对应 Adapter 里的 getItemViewType：
 *   每种数据类型 → 对应一种 ViewHolder 和布局
 */
sealed class WanasHomeItem {

    /**
     * 用户卡片数据
     */
    data class UserItem(
        val id: Int,
        val name: String,
        val avatarEmoji: String,
        val msgCount: Int,
        val country: String
    ) : WanasHomeItem()

    /**
     * Banner 广告卡数据
     */
    data class BannerItem(
        val title: String,
        val subtitle: String
    ) : WanasHomeItem()

    /**
     * 列表底部"加载中"占位符
     *
     * ★ object（单例）而不是 data class：
     *   LoadingItem 只表示"正在加载"这个状态，没有任何数据字段
     *   全局只需要一个实例，用 object 最合适
     *
     * ★ sealed class 要求所有子类必须在同一文件内声明
     *   新加这一行，编译器会强制让所有 when(item) 表达式也处理 LoadingItem
     *   → getItemViewType / onBindViewHolder 漏写会报错，不会运行时崩溃
     */
    object LoadingItem : WanasHomeItem()
}
