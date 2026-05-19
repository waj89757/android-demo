package com.example.demo01

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ★ RetrofitClient —— 单例模式，全局只有一个 Retrofit 实例
 *
 * 后端类比：
 * - 这就像你用 Flask 定义 app = Flask(__name__)，全局只有一个 app 实例
 * - Retrofit 实例就是你的"HTTP 客户端"，配置好 base URL 和 JSON 解析器
 * - 所有 API 请求都通过这一个实例发出去
 *
 * ★ 三层结构：
 * 1. OkHttp —— 底层 HTTP 客户端（类似 Python 的 requests 库）
 *    负责：TCP连接、发送请求、接收响应、日志打印
 *
 * 2. GsonConverterFactory —— JSON 解析器（类似后端的 Jackson/Gson）
 *    负责：把后端返回的 JSON 字符串 → 自动转成 Kotlin 对象（Post）
 *
 * 3. Retrofit —— 上层封装（类似后端的 HTTP 客户端封装）
 *    负责：根据 ApiService 接口定义，自动组装请求 URL 和参数
 *
 * ★ 日志拦截器（HttpLoggingInterceptor）：
 *   相当于后端的 request/response 日志中间件
 *   打开后可以在 Logcat 里看到完整的请求 URL、响应 JSON
 *   调试时非常有用，上线前要关掉（设为 NONE）
 */
object RetrofitClient {

    // ★ BASE_URL —— 后端 API 的根地址
    // 所有 ApiService 里的 @GET("posts") 会拼在这个后面
    // 最终请求地址 = BASE_URL + "posts" = https://jsonplaceholder.typicode.com/posts
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // ★ 日志拦截器 —— 打印 HTTP 请求/响应的完整日志
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // 打印完整请求体和响应体
    }

    // ★ OkHttp 客户端 —— 底层 HTTP 连接器
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)   // 加日志拦截器
        .build()

    // ★ Retrofit 实例 —— 核心对象
    // 传入三个参数：
    //   1. baseUrl：后端根地址
    //   2. client：底层 HTTP 客户端
    //   3. converterFactory：JSON → Kotlin 对象的转换器
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ★ 创建 ApiService 实例
    // Retrofit 根据接口定义，自动生成实现类（动态代理）
    // 类似后端的 RPC 代理——你只定义接口，框架帮你生成实现
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}