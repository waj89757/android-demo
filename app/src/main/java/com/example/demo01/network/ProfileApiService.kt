package com.example.demo01.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit 接口定义
 *
 * 这里定义的是"和后端约定好的接口契约"：
 * - 接口路径：POST /api/profile/save
 * - 请求体：ProfileRequest（JSON）
 * - 响应体：Response<Void>（我们只关心 HTTP 状态码，不解析响应体）
 */
interface ProfileApiService {

    @POST("api/profile/save")
    suspend fun saveProfile(@Body request: ProfileRequest): Response<Void>
}
