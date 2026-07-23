package com.example.demo

/**
 * DiffUtil 演示用的商品数据类。
 *
 * - id：唯一标识，DiffUtil 用它判断"是不是同一个商品"（areItemsTheSame）
 * - name/price/stock：会变化的内容字段，DiffUtil 用它们判断"内容变了吗"（areContentsTheSame）
 *
 * 用 data class 的好处：自动生成 equals()，== 会逐字段比较，
 * areContentsTheSame 里直接 old == new 即可。
 */
data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val stock: Int
)
