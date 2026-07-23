package com.example.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ DeepLink Demo —— 接收并解析 DeepLink ★★★
 *
 * 这个 Activity 演示两件事：
 *   1. 如何在 AndroidManifest 里声明接收 DeepLink
 *   2. 如何在代码里解析 URL 拿到参数
 *
 * ★ 支持的 DeepLink 格式（在 Manifest 里声明）：
 *
 *   demo01://post?id=123&title=HelloWorld
 *   │        │    │
 *   │        │    └── Query 参数（?后面）
 *   │        └─────── host（路径）
 *   └──────────────── scheme（协议头，就是 App 的标识）
 *
 * ★ 类比 HTTP URL：
 *   https://api.example.com/post?id=123&title=HelloWorld
 *   │       │               │    └── 参数
 *   │       │               └─────── path
 *   │       └───────────────────────  host
 *   └───────────────────────────────  scheme
 *
 * ★ 触发方式：
 *   方式1：点 MainActivity 的"模拟触发 DeepLink"按钮（App内部触发）
 *   方式2：adb shell am start -W -a android.intent.action.VIEW \
 *            -d "demo01://post?id=999&title=ADB触发" com.example.demo01
 *          （命令行模拟外部触发，最接近真实场景）
 */
class DeepLinkDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link_demo)

        val tvSource   = findViewById<TextView>(R.id.tv_source)
        val tvUrl      = findViewById<TextView>(R.id.tv_url)
        val tvParams   = findViewById<TextView>(R.id.tv_params)
        val tvUrlParts = findViewById<TextView>(R.id.tv_url_parts)

        // ★ 核心：从 Intent 里拿 DeepLink 的 URI
        //
        // 当 Android 系统因为 DeepLink 启动这个 Activity 时：
        //   intent.action = "android.intent.action.VIEW"
        //   intent.data   = Uri("demo01://post?id=123&title=xxx")
        //
        // 当 MainActivity 直接 startActivity 启动时：
        //   intent.action = null 或 "android.intent.action.MAIN"
        //   intent.data   = 我们手动放进去的 Uri（模拟 DeepLink）
        val uri: Uri? = intent.data

        if (uri == null) {
            // 没有 URI，说明不是通过 DeepLink 打开的
            tvSource.text = "触发来源：直接启动（非 DeepLink）"
            tvUrl.text = "完整 URL：无"
            tvParams.text = "解析出的参数：无"
            tvUrlParts.text = "URL 结构解析：无"
        } else {
            // ★ 有 URI，是通过 DeepLink 打开的
            // 判断是外部触发还是 App 内部模拟
            val source = if (intent.action == Intent.ACTION_VIEW) "外部触发（系统路由）"
                         else "App 内部模拟触发"
            tvSource.text = "触发来源：$source"

            // ★ 显示完整 URL
            tvUrl.text = "完整 URL：\n$uri"

            // ★ 解析参数
            // uri.getQueryParameter("key") 等价于解析 URL 的 query string
            // demo01://post?id=123&title=Hello
            //                 ↑              ↑
            //          getQueryParameter("id")  getQueryParameter("title")
            val id    = uri.getQueryParameter("id") ?: "未传"
            val title = uri.getQueryParameter("title") ?: "未传"
            tvParams.text = "解析出的参数：\n  id    = $id\n  title = $title"

            // ★ 拆解 URL 各部分，帮助理解结构
            // demo01://post?id=123&title=Hello
            //   scheme = "demo01"
            //   host   = "post"
            //   query  = "id=123&title=Hello"
            tvUrlParts.text = """URL 结构解析：
  scheme（协议/App标识）= ${uri.scheme}
  host  （路径/页面名）  = ${uri.host}
  path  （子路径）       = ${uri.path?.ifEmpty { "（无）" } ?: "（无）"}
  query （参数字符串）   = ${uri.query ?: "（无）"}"""
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
