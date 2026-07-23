package com.example.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit

/**
 * Mock HTTP 拦截器
 *
 * 原理：实现 OkHttp 的 Interceptor 接口，在请求真正发出网络之前拦截它，
 *       直接构造一个假的 HTTP Response 返回，请求不会真正出网络。
 *
 * 如何切换真实请求：
 *   - 从 RetrofitClient 的 OkHttpClient 里移除 MockInterceptor 即可
 *   - 业务代码（ProfileActivity、ProfileApiService）一行不用改
 *
 * 模拟控制：
 *   - MOCK_SUCCESS_RATE：模拟随机失败概率（0.0=全失败，1.0=全成功）
 *   - MOCK_DELAY_MS：模拟网络延迟（毫秒）
 */
class MockInterceptor : Interceptor {

    companion object {
        private const val TAG = "MockInterceptor"

        /** 成功率：1.0 = 100% 成功，0.8 = 80% 成功 */
        private const val MOCK_SUCCESS_RATE = 1.0

        /** 模拟网络延迟（毫秒） */
        private const val MOCK_DELAY_MS = 800L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 打印拦截到的请求信息
        Log.d(TAG, "=== MockInterceptor 拦截请求 ===")
        Log.d(TAG, "URL    : ${request.url}")
        Log.d(TAG, "Method : ${request.method}")

        // 模拟网络延迟
        TimeUnit.MILLISECONDS.sleep(MOCK_DELAY_MS)

        // 根据成功率决定返回 200 还是 500
        val isSuccess = Math.random() < MOCK_SUCCESS_RATE
        val statusCode = if (isSuccess) 200 else 500
        val message = if (isSuccess) "OK" else "Internal Server Error"

        Log.d(TAG, "Mock Response: $statusCode $message")
        Log.d(TAG, "================================")

        // 构造假的 HTTP Response
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message(message)
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
