package com.example.demo01

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewDemoActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_demo)

        webView = findViewById(R.id.web_view)

        // 1. 开启 JS 支持（默认关闭）
        webView.settings.javaScriptEnabled = true

        // 2. ★ 注入 Bridge 对象到 JS 世界
        //    JS 里就可以用 window.NativeBridge.xxx() 调 Native 方法
        webView.addJavascriptInterface(NativeBridge(), "NativeBridge")

        // 3. 设置 WebViewClient + 离线包拦截
        webView.webViewClient = object : WebViewClient() {

            /**
             * ★ shouldInterceptRequest：WebView 每次发网络请求前都会调这里
             *
             * 返回 null  → WebView 正常发网络请求
             * 返回 Response → WebView 直接用这个内容，不发网络请求
             *
             * 离线包原理：检查本地有没有对应文件，有就直接返回本地内容
             */
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()

                // ★ 模拟离线包拦截：
                // 假设 https://m.example.com/offline/page.html 是要访问的远程页面
                // 但我们本地有这个文件，直接返回本地内容
                if (url.contains("m.example.com/offline/page.html")) {
                    android.util.Log.d("WebView", "★ 拦截到请求：$url → 返回本地离线内容")

                    // 把本地 HTML 字符串包装成 Response 返回
                    // 实际项目里这里读的是本地文件（解压后的离线包）
                    val offlineHtml = buildOfflineHtml()
                    val inputStream = offlineHtml.byteInputStream(Charsets.UTF_8)
                    return WebResourceResponse("text/html", "UTF-8", inputStream)
                }

                // 其他请求放行，正常走网络
                return null
            }
        }

        // 4. 直接加载 HTML 字符串（最简单，不需要服务器）
        webView.loadDataWithBaseURL(null, buildHtml(), "text/html", "UTF-8", null)

        // ★ 离线包演示按钮：加载一个「假的远程 URL」，但被拦截返回本地内容
        findViewById<Button>(R.id.btn_offline).setOnClickListener {
            // 这个 URL 根本不存在（没有真正的服务器）
            // 但 shouldInterceptRequest 会拦截它，返回本地内容
            webView.loadUrl("https://m.example.com/offline/page.html")
            Toast.makeText(this, "尝试加载远程 URL，看看会不会被拦截...", Toast.LENGTH_SHORT).show()
        }

        // ★ Native 主动调 JS：把当前时间注入到 H5 页面
        findViewById<Button>(R.id.btn_send_to_js).setOnClickListener {
            val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            // evaluateJavascript：在 WebView 里执行一段 JS 代码
            // 这里调用 H5 页面里定义的 receiveFromNative() 函数
            webView.evaluateJavascript("receiveFromNative('Native 发来消息，时间：$time')", null)
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    /**
     * ★ JSBridge 类：注入到 JS 世界的对象
     * JS 里调 NativeBridge.showToast("hello") 会直接触发这里的 showToast()
     *
     * 注意：@JavascriptInterface 必须加，否则 Android 4.2+ 会拒绝调用
     *      这些方法在子线程执行，不能直接更新 UI，要 runOnUiThread
     */
    inner class NativeBridge {

        // JS 调用：NativeBridge.showToast("消息")
        @JavascriptInterface
        fun showToast(message: String) {
            // 注意：这里在子线程，Toast 需要切回主线程
            runOnUiThread {
                Toast.makeText(this@WebViewDemoActivity, "H5 说：$message", Toast.LENGTH_SHORT).show()
            }
        }

        // JS 调用：NativeBridge.getAppVersion()，返回值给 JS
        @JavascriptInterface
        fun getAppVersion(): String {
            return "1.0.0-demo"
        }
    }

    // H5 页面的 HTML（内嵌在代码里，最简单的形式）
    private fun buildHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: sans-serif; padding: 20px; background: #f0f4f8; }
                h3 { color: #1565C0; }
                button {
                    display: block; width: 100%; padding: 12px;
                    margin: 10px 0; font-size: 16px;
                    background: #1565C0; color: white;
                    border: none; border-radius: 8px; cursor: pointer;
                }
                #result {
                    margin-top: 16px; padding: 12px;
                    background: white; border-radius: 8px;
                    border-left: 4px solid #1565C0;
                    font-size: 14px; color: #333;
                }
            </style>
        </head>
        <body>
            <h3>★ 这里是 H5 世界（WebView 里）</h3>
            <p style="color:#666; font-size:13px;">
                JS 沙盒环境，默认不能调相机/GPS 等<br>
                通过 JSBridge 可以调用 Native 能力
            </p>

            <!-- JS 调 Native：弹 Toast -->
            <button onclick="callNativeToast()">
                JS → Native：弹 Toast
            </button>

            <!-- JS 调 Native：拿版本号 -->
            <button onclick="callNativeGetVersion()">
                JS → Native：获取 App 版本号
            </button>

            <!-- 接收 Native 发来的消息 -->
            <div id="result">等待 Native 发消息...</div>

            <script>
                // ★ JS 调 Native（方向：JS → Native）
                // window.NativeBridge 是 addJavascriptInterface 注入的对象
                function callNativeToast() {
                    window.NativeBridge.showToast("你好，我是 H5！");
                }

                function callNativeGetVersion() {
                    // getAppVersion() 有返回值，JS 直接拿到
                    var version = window.NativeBridge.getAppVersion();
                    document.getElementById('result').innerHTML =
                        '★ Native 返回版本号：' + version;
                }

                // ★ Native 调 JS（方向：Native → JS）
                // Native 调 webView.evaluateJavascript("receiveFromNative(...)", null)
                // 所以 H5 里要定义这个全局函数
                function receiveFromNative(message) {
                    document.getElementById('result').innerHTML =
                        '★ 收到 Native 消息：' + message;
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    // 模拟离线包里的本地 HTML 内容
    private fun buildOfflineHtml() = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: sans-serif; padding: 20px; background: #E8F5E9; }
                h2 { color: #2E7D32; }
                .tag {
                    display: inline-block; background: #2E7D32; color: white;
                    padding: 4px 12px; border-radius: 20px; font-size: 13px;
                }
                p { color: #444; line-height: 1.6; }
            </style>
        </head>
        <body>
            <span class="tag">★ 离线包内容</span>
            <h2>这个页面来自本地！</h2>
            <p>
                你请求的 URL 是：<br>
                <code>https://m.example.com/offline/page.html</code>
            </p>
            <p>
                这个域名根本不存在，但 <code>shouldInterceptRequest</code>
                拦截了请求，返回了本地文件内容。
            </p>
            <p>
                ✅ 没有发出任何网络请求<br>
                ✅ 加载速度 = 读本地文件<br>
                ✅ 这就是离线包的核心原理
            </p>
        </body>
        </html>
    """.trimIndent()
}
