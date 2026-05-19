package com.example.demo01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 传统标准适配器，最简单最稳，无任何内部类找不到问题
class MyTextAdapter(
    private val data: MutableList<String>
) : RecyclerView.Adapter<MyTextAdapter.Holder>() {

    // 条目视图持有者
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_text, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.tv.text = data[position]
    }

    override fun getItemCount(): Int = data.size

    // 刷新数据方法
    fun refreshData(newList: List<String>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }
}