package com.example.demo01

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * ★★★ DiffUtil / ListAdapter 演示页面 ★★★
 *
 * 页面上放 4 个按钮，每个按钮只做一件事：生成一个"新列表" → submitList。
 * 从头到尾没有手动调用任何 notifyXxx()，全部由 DiffUtil 自动完成。
 *
 * 观察点：
 *   [改一条] → 只有 iPhone 那一行闪动更新，其余纹丝不动（对比全刷会全部闪）
 *   [插入]   → 新商品滑入，下方平滑下移
 *   [删除]   → 第一个淡出，下方平滑上移
 *   [打乱]   → 所有 item 平滑移动到新位置（不是瞬间跳）
 */
class DiffUtilDemoActivity : AppCompatActivity() {

    private val diffAdapter = ProductDiffAdapter()

    // 当前数据（每次基于它生成新 list 再 submit）
    private var data = listOf(
        Product(1, "iPhone", 5999, 10),
        Product(2, "iPad", 3999, 8),
        Product(3, "MacBook", 9999, 5),
        Product(4, "AirPods", 1299, 20),
        Product(5, "Watch", 2999, 15)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diff_demo)

        findViewById<RecyclerView>(R.id.rv_products).apply {
            layoutManager = LinearLayoutManager(this@DiffUtilDemoActivity)
            adapter = diffAdapter
        }

        // 首次提交数据
        submit(data)

        // 改一条：只改 iPhone(id=1) 的价格和库存 → DiffUtil 只更新那一行
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            data = data.map {
                if (it.id == 1) it.copy(price = it.price + 100, stock = it.stock - 1)
                else it
            }
            submit(data)
        }

        // 插入一条：在第 3 位插入新商品 → 插入动画
        findViewById<Button>(R.id.btn_insert).setOnClickListener {
            val newList = data.toMutableList()
            val insertIndex = if (data.size >= 2) 2 else data.size
            newList.add(insertIndex, Product(id = (100..999).random(), name = "新品耳机", price = 599, stock = 30))
            data = newList
            submit(data)
        }

        // 删除一条：删掉第一个 → 删除动画 + 下方上移
        findViewById<Button>(R.id.btn_delete).setOnClickListener {
            if (data.isNotEmpty()) {
                data = data.drop(1)
                submit(data)
            }
        }

        // 打乱顺序：shuffle → item 平滑移动到新位置（areItemsTheSame 认得 id，播放移动动画）
        findViewById<Button>(R.id.btn_shuffle).setOnClickListener {
            data = data.shuffled()
            submit(data)
        }
    }

    // ★ 关键：每次都传一个"新的 list 引用"（toList 拷贝一份）
    //   如果直接改原 list 再 submit 同一个引用，DiffUtil 认为新旧相同，可能不刷新
    private fun submit(list: List<Product>) {
        diffAdapter.submitList(list.toList())
    }
}
