package com.example.network

import com.google.gson.annotations.SerializedName

/**
 * 保存 Profile 的请求体，Gson 会把字段名序列化成 JSON key
 *
 * 示例 JSON：
 * {
 *   "gender": "Male",
 *   "category": "General",
 *   "mobile": "9849012345",
 *   "parent_email": "parent@gmail.com",
 *   "father_name": "John Doe",
 *   "mother_name": "Jane Doe",
 *   "street": "123 Main St",
 *   "city": "Mumbai",
 *   "state": "Maharashtra"
 * }
 */
data class ProfileRequest(
    @SerializedName("gender")       val gender: String,
    @SerializedName("category")     val category: String,
    @SerializedName("mobile")       val mobile: String,
    @SerializedName("parent_email") val parentEmail: String,
    @SerializedName("father_name")  val fatherName: String,
    @SerializedName("mother_name")  val motherName: String,
    @SerializedName("street")       val street: String,
    @SerializedName("city")         val city: String,
    @SerializedName("state")        val state: String
)
