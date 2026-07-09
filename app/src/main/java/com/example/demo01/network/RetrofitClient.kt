package com.example.demo01.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 单例
 *
 * OkHttp 责任链（请求经过的顺序）：
 *   LoggingInterceptor（打印请求/响应日志）
 *       ↓
 *   MockInterceptor（拦截，返回假响应，请求不出网络）
 *       ↓
 *   [如果移除 MockInterceptor，请求会真正发往网络]
 */
object RetrofitClient {

    /** 假的 baseUrl，MockInterceptor 会拦截，真实情况下替换成真正的后端地址 */
    private const val BASE_URL = "https://api.example.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // 打印完整请求体和响应体
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)   // 先打日志
        .addInterceptor(MockInterceptor())    // 再拦截（移除此行 = 真实请求）
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** 获取 ProfileApiService 实例 */
    val profileApiService: ProfileApiService by lazy {
        retrofit.create(ProfileApiService::class.java)
    }
}
