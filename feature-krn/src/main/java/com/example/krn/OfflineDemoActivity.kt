package com.example.krn

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ 离线包 Demo ★★★
 *
 * 完整真实流程：
 *   assets/offline_v1.zip（模拟从服务端下载的离线包）
 *     ↓ 解压
 *   /data/data/app/files/offline/activity_v1/
 *     ├── manifest.json    ← URL 映射表
 *     ├── index.html
 *     └── static/
 *           ├── main.css
 *           └── app.js
 *     ↓ shouldInterceptRequest 拦截
 *   WebView 加载远程 URL，实际读本地文件，不走网络
 */
class OfflineDemoActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvLog: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_demo)

        webView = findViewById(R.id.web_view_offline)
        tvLog   = findViewById(R.id.tv_offline_log)

        webView.settings.javaScriptEnabled = true

        // ★ 第一步：初始化离线包（解压 zip + 加载 manifest）
        log("正在初始化离线包...")
        OfflinePackageManager.init(this)
        log("离线包就绪，映射表：")
        OfflinePackageManager.getUrlMap().forEach { (url, path) ->
            log("  $url\n  → ...${path.takeLast(40)}")
        }

        // ★ 第二步：设置 WebViewClient，关键在 shouldInterceptRequest
        webView.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()

                // ★ 查映射表（从 manifest.json 加载的）
                val localFile = OfflinePackageManager.getLocalFile(url)

                return if (localFile != null) {
                    log("⚡ 拦截命中: ${url.substringAfterLast("/")}")
                    // 返回本地文件内容，WebView 不发网络请求
                    WebResourceResponse(
                        getMimeType(url),
                        "UTF-8",
                        localFile.inputStream()
                    )
                } else {
                    // 不在离线包里，放行走网络
                    null
                }
            }
        }

        // ★ 第三步：加载"远程"URL（实际上会被离线包拦截）
        findViewById<Button>(R.id.btn_load_offline).setOnClickListener {
            log("\n--- 开始加载 ---")
            log("请求 URL: https://m.example.com/activity/v1/index.html")
            webView.loadUrl("https://m.example.com/activity/v1/index.html")
        }

        // 重新解压（模拟"收到新版本覆盖安装"）
        findViewById<Button>(R.id.btn_reinstall).setOnClickListener {
            log("\n--- 重新安装离线包 ---")
            OfflinePackageManager.forceReinstall(this)
            log("重装完成，映射表更新")
        }

        findViewById<Button>(R.id.btn_back_offline).setOnClickListener { finish() }
    }

    private fun getMimeType(url: String): String = when {
        url.endsWith(".html") -> "text/html"
        url.endsWith(".css")  -> "text/css"
        url.endsWith(".js")   -> "application/javascript"
        url.endsWith(".png")  -> "image/png"
        url.endsWith(".jpg")  -> "image/jpeg"
        else                  -> "text/plain"
    }

    private fun log(msg: String) {
        runOnUiThread {
            tvLog.append("$msg\n")
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
