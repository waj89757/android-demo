package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * ★ ApiService —— Retrofit 的 API 接口定义
 *
 * 后端类比：
 * - 这个接口就像你用 Python requests 库定义的 API 调用方法
 * - 但 Retrofit 不需要你手写 HTTP 请求代码，只需要声明"我要调哪个 URL"
 * - Retrofit 自动帮你发请求、解析 JSON、返回 Kotlin 对象
 *
 * ★ @GET 注解 = 声明这是一个 GET 请求
 * ★ @Query 注解 = 声明 URL 的查询参数（?_limit=5）
 *
 * 示例：
 *   @GET("posts")              → 请求 /posts
 *   @GET("posts/{id}")         → 请求 /posts/1（路径参数）
 *   @GET("posts?_limit=5")     → 请求 /posts?_limit=5
 *
 * ★ suspend 关键字 = Kotlin 协程的异步方法标记
 *   调用 suspend 函数不会阻塞当前线程，而是"挂起"等结果
 *   类似 Python 的 await，但更轻量（协程不是线程）
 *
 *   没有 suspend → 调用时会阻塞线程等待结果（同步）
 *   有 suspend   → 调用时会挂起协程，不阻塞线程（异步）
 */
interface ApiService {

    // ★ 获取帖子列表 —— GET https://jsonplaceholder.typicode.com/posts?_limit=5
    @GET("posts")
    suspend fun getPosts(@Query("_limit") limit: Int = 5): List<Post>

    // ★ 获取单个帖子 —— GET https://jsonplaceholder.typicode.com/posts/1
    @GET("posts/{id}")
    suspend fun getPostById(@retrofit2.http.Path("id") id: Int): Post
}