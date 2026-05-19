package com.example.demo01

import com.google.gson.annotations.SerializedName

/**
 * ★ Post 数据类 —— 对应后端 API 返回的 JSON 结构
 *
 * JSONPlaceholder API 返回的数据格式：
 * {
 *   "userId": 1,
 *   "id": 1,
 *   "title": "sunt aut facere repellat provident",
 *   "body": "quia et suscipit..."
 * }
 *
 * ★ Gson 的作用：自动把 JSON 转成 Kotlin 对象
 * 就像后端用 Jackson/Gson 把 HTTP 响应 JSON 转成 Java/Kotlin 对象
 * @SerializedName 是 Gson 的注解，当 JSON 字段名和 Kotlin 属性名不同时用
 */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)