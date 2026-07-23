package com.example.demo

import kotlinx.coroutines.delay

/**
 * ★ WanasRepository —— 数据层，负责"怎么拿数据"
 *
 * 真实项目里这里会调 Retrofit 请求服务器
 * 现在 mock：延迟 800ms 模拟网络，然后返回假数据
 *
 * ★ 为什么要有 Repository 这一层（而不是直接在 ViewModel 里写 mock）：
 *
 *   职责分离：ViewModel 只管"持有数据 + 暴露给 UI"，不关心数据从哪来
 *   未来切换：想从 mock 换成真实 API，只改 Repository 这一个文件
 *   ViewModel 代码一行不用动
 *
 * ┌─────────┐   调用   ┌────────────┐   调用   ┌──────────────┐
 * │Fragment │ ──────▶ │ ViewModel  │ ──────▶  │ Repository   │
 * │(UI层)   │         │ (状态管理)  │          │ (数据来源)    │
 * └─────────┘         └────────────┘          └──────────────┘
 *
 * ★ suspend 函数：
 *   只能在协程里调用
 *   delay(800) 挂起协程 800ms，主线程不阻塞（用户界面不卡）
 *   800ms 后协程恢复，返回数据
 */
class WanasRepository {

    /**
     * 获取第 page 页的数据
     *
     * @param page  从 1 开始，每页 7 条
     * @return      7 条 WanasHomeItem，包含用户卡和 Banner 的混排
     */
    suspend fun fetchPage(page: Int): List<WanasHomeItem> {
        // 模拟网络延迟
        delay(800)

        // 每页 7 条，id 从 (page-1)*7+1 开始，保证全局唯一
        val startId = (page - 1) * 7 + 1

        // 3页之后返回空列表，模拟"没有更多了"
        if (page > 3) return emptyList()

        return when (page) {
            1 -> listOf(
                WanasHomeItem.UserItem(startId,     "سندريلا",           "👩",  26, "🇧🇭"),
                WanasHomeItem.UserItem(startId + 1, "🍒 Mira 🍒",        "👩‍🦱", 26, "🇸🇦"),
                WanasHomeItem.UserItem(startId + 2, "عشق 🕺",            "👩‍🦳", 37, "🇪🇬"),
                WanasHomeItem.BannerItem("✨ ميزات جديدة وصلت!", "💜 غرفتك، أجواؤك"),
                WanasHomeItem.UserItem(startId + 3, "روح 🔥",            "👩‍🎤", 31, "🇪🇬"),
                WanasHomeItem.UserItem(startId + 4, "Ŝàrà",              "👸",  28, "🇪🇬"),
                WanasHomeItem.UserItem(startId + 5, "لولو وكاله",        "👧",  22, "🇸🇦"),
            )
            2 -> listOf(
                WanasHomeItem.UserItem(startId,     "Nora ⭐",            "🧕",  19, "🇦🇪"),
                WanasHomeItem.UserItem(startId + 1, "Layla 🌙",           "👩‍💼", 34, "🇶🇦"),
                WanasHomeItem.UserItem(startId + 2, "Hana 🌸",            "🧑",  24, "🇰🇼"),
                WanasHomeItem.BannerItem("🎵 غرف الموسيقى الجديدة", "🎶 انضم الآن"),
                WanasHomeItem.UserItem(startId + 3, "Reem 💫",            "👩‍🎨", 29, "🇴🇲"),
                WanasHomeItem.UserItem(startId + 4, "زهرة 🌺",            "🧕",  33, "🇮🇶"),
                WanasHomeItem.UserItem(startId + 5, "Dana 🦋",            "👩‍🦰", 27, "🇯🇴"),
            )
            3 -> listOf(
                WanasHomeItem.UserItem(startId,     "Salma 🌻",           "👩",  21, "🇸🇩"),
                WanasHomeItem.UserItem(startId + 1, "Noura 🌈",           "🧑",  31, "🇲🇦"),
                WanasHomeItem.UserItem(startId + 2, "Aya ✨",             "👩‍🎓", 25, "🇱🇧"),
                WanasHomeItem.BannerItem("🏆 أفضل الغرف هذا الأسبوع", "👑 تصفح الآن"),
                WanasHomeItem.UserItem(startId + 3, "Lina 🍀",            "👩‍🔬", 30, "🇸🇾"),
                WanasHomeItem.UserItem(startId + 4, "Mona 🌙",            "🧕",  26, "🇹🇳"),
                WanasHomeItem.UserItem(startId + 5, "Sana 🌟",            "👩‍💻", 28, "🇩🇿"),
            )
            else -> emptyList()
        }
    }
}
