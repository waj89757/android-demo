package com.example.demo01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/**
 * 首页列表 Adapter —— 多类型 RecyclerView 的完整示范
 *
 * ★ 核心知识点：多类型 item 的三步走
 *
 * 第一步：定义 item 类型常量
 *   VIEW_TYPE_USER    = 0  对应用户卡片
 *   VIEW_TYPE_BANNER  = 1  对应 Banner 广告卡
 *   VIEW_TYPE_LOADING = 2  对应底部加载中占位符
 *
 * 第二步：重写 getItemViewType(position)
 *   根据数据类型返回对应的常量
 *   RecyclerView 用这个值决定"回收池"：
 *     类型0的卡片只会被类型0的 ViewHolder 复用
 *     类型1的卡片只会被类型1的 ViewHolder 复用
 *
 * 第三步：onCreateViewHolder 根据 viewType inflate 不同布局
 *   viewType=0 → inflate item_wanas_user.xml   → UserViewHolder
 *   viewType=1 → inflate item_wanas_banner.xml → BannerViewHolder
 *   viewType=2 → inflate item_wanas_loading.xml → LoadingViewHolder
 *
 * ★ ViewHolder 的本质：
 *   缓存 item 布局里的所有子 View，避免每次 onBindViewHolder
 *   都要 findViewById（这是 RecyclerView 比 ListView 快的关键之一）
 */
class WanasHomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ─── 当前列表数据（含 LoadingItem 占位符）────────────────────
    private val displayItems = mutableListOf<WanasHomeItem>()

    // ─── item 类型常量 ───────────────────────────────────────────
    companion object {
        const val VIEW_TYPE_USER    = 0
        const val VIEW_TYPE_BANNER  = 1
        const val VIEW_TYPE_LOADING = 2
    }

    // ─── ViewHolder 定义（每种类型一个）──────────────────────────

    /**
     * 用户卡片的 ViewHolder
     * 构造时就把所有子 View 找出来缓存，之后 bind 时直接用
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar:   TextView = itemView.findViewById(R.id.tv_avatar)
        val tvName:     TextView = itemView.findViewById(R.id.tv_user_name)
        val tvMsgCount: TextView = itemView.findViewById(R.id.tv_msg_count)
        val btnHi:      TextView = itemView.findViewById(R.id.btn_hi)
    }

    /** Banner 卡片的 ViewHolder */
    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_banner_title)
        val tvSub:   TextView = itemView.findViewById(R.id.tv_banner_sub)
    }

    /**
     * 加载中占位符的 ViewHolder
     * 布局只有一个 ProgressBar，没有要缓存的子 View
     */
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ─── 对外接口：Fragment 通过这两个方法更新列表 ────────────────

    /**
     * ★ 追加一页新数据
     *
     * 调用时机：ViewModel 拿到新一页数据后，Fragment 调这个方法
     *
     * 原理：
     *   先移除旧的 LoadingItem（如果有）
     *   → 追加新数据
     *   → 如果还有更多数据，在末尾加 LoadingItem 占位符
     *
     * @param newPage    这一页的 7 条数据
     * @param hasMore    是否还有下一页（控制是否显示底部 loading）
     */
    fun appendPage(newPage: List<WanasHomeItem>, hasMore: Boolean) {
        // 先移除末尾的 LoadingItem（避免重复）
        if (displayItems.lastOrNull() is WanasHomeItem.LoadingItem) {
            val lastIndex = displayItems.lastIndex
            displayItems.removeAt(lastIndex)
            notifyItemRemoved(lastIndex)
        }

        // 追加新数据
        val insertStart = displayItems.size
        displayItems.addAll(newPage)
        notifyItemRangeInserted(insertStart, newPage.size)

        // 如果还有更多，末尾加 Loading 占位符
        if (hasMore) {
            displayItems.add(WanasHomeItem.LoadingItem)
            notifyItemInserted(displayItems.lastIndex)
        }
    }

    // ─── RecyclerView 三大必须实现的方法 ────────────────────────

    override fun getItemCount(): Int = displayItems.size

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is WanasHomeItem.UserItem   -> VIEW_TYPE_USER
            is WanasHomeItem.BannerItem -> VIEW_TYPE_BANNER
            is WanasHomeItem.LoadingItem -> VIEW_TYPE_LOADING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.item_wanas_user, parent, false)
                UserViewHolder(view)
            }
            VIEW_TYPE_BANNER -> {
                val view = inflater.inflate(R.layout.item_wanas_banner, parent, false)
                BannerViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.item_wanas_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
Ï
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {

            is WanasHomeItem.UserItem -> {
                val h = holder as UserViewHolder
                h.tvAvatar.text   = item.avatarEmoji
                h.tvName.text     = item.name
                h.tvMsgCount.text = "${item.country}  💬${item.msgCount}"
                // ★ 用 bindingAdapterPosition 而不是闭包里的 position
                //   原因：ViewHolder 被复用后，position 参数是旧值
                //   bindingAdapterPosition 是点击那一刻的真实位置
                h.btnHi.setOnClickListener {
                    val current = holder.bindingAdapterPosition
                    if (current == RecyclerView.NO_ID.toInt()) return@setOnClickListener
                    Toast.makeText(it.context, "Hi ${item.name}!", Toast.LENGTH_SHORT).show()
                }
            }

            is WanasHomeItem.BannerItem -> {
                val h = holder as BannerViewHolder
                h.tvTitle.text = item.title
                h.tvSub.text   = item.subtitle
            }

            // LoadingItem 没有数据要填，ViewHolder 只有 ProgressBar（自动旋转）
            is WanasHomeItem.LoadingItem -> { /* nothing to bind */ }
        }
    }
}
