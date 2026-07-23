package com.example.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * ★★★ DiffUtil / ListAdapter 演示 Adapter ★★★
 *
 * 与普通 RecyclerView.Adapter 的区别：
 *   1. 继承 ListAdapter（而不是 RecyclerView.Adapter）
 *   2. 构造时传入一个 DiffUtil.ItemCallback
 *   3. 不用自己维护 list、不用写 getItemCount()、不用手动调 notifyXxx()
 *   4. 数据更新只需调 submitList(新list)，差异计算 + 精确刷新 + 动画全自动
 *
 * submitList 之后发生什么：
 *   ListAdapter 在后台线程跑 DiffUtil.calculateDiff(旧, 新)
 *   → 逐对调用下面的 areItemsTheSame / areContentsTheSame
 *   → 算出"谁插入/谁删除/谁移动/谁更新"的变更清单
 *   → 回到主线程自动调用 notifyItemInserted/Removed/Moved/Changed
 *   → RecyclerView 只刷新变化的 item，并播放增删移动画
 */
class ProductDiffAdapter :
    ListAdapter<Product, ProductDiffAdapter.VH>(ProductDiffCallback()) {

    // ---------- DiffUtil 回调：只需回答两个问题 ----------
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

        // 问题1：这两个是不是"同一个"商品？（比唯一 id）
        // 返回 true → 认为是同一个 item，接着问问题2
        // 返回 false → 认为是完全不同的 item（一个删除、一个新增）
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        // 问题2：既然是同一个商品，它的内容变了吗？（data class == 比所有字段）
        // 返回 false → 内容变了 → 触发 onBindViewHolder 就地更新（带更新动画）
        // 返回 true  → 完全没变 → 不刷新这一行
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    // ---------- ViewHolder：把子控件引用 find 出来握住 ----------
    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvStock: TextView = itemView.findViewById(R.id.tv_product_stock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // ListAdapter 提供 getItem(position)，内部维护当前列表，不用自己存
        val p = getItem(position)
        holder.tvName.text = p.name
        holder.tvPrice.text = "￥${p.price}"
        holder.tvStock.text = "库存 ${p.stock}"
    }

    // 注意：不需要重写 getItemCount()，ListAdapter 会自动根据当前列表返回
}
