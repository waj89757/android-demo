package com.example.krn

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ RN 容器 Activity（URI 路由版）★★★
 *
 * 真实 RN 的工作方式：
 *   1. Native 路由表把 URI 映射到这个 Activity
 *   2. 这个 Activity 解析 URI，提取 route + params
 *   3. 把 route/params 注入 JS（window.ROUTE_CONFIG）
 *   4. JS Router（router_bundle.js）读取 ROUTE_CONFIG，渲染对应页面
 *
 * 支持两种启动方式：
 *   A. Native 内部跳转：startActivity(Intent) + putExtra("uri", "myapp://...")
 *   B. DeepLink 外部跳转：浏览器/Push 直接打开 myapp://... URI
 *      （AndroidManifest 里配置 intent-filter）
 *
 * 支持的 URI：
 *   myapp://product/detail?id=123&name=蓝牙耳机
 *   myapp://user/profile?userId=u_10086
 *   myapp://order/list
 */
class RNContainerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvRoute: TextView
    private lateinit var tvLog: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rn_container)

        webView = findViewById(R.id.wv_rn)
        tvRoute = findViewById(R.id.tv_current_route)
        tvLog   = findViewById(R.id.tv_rn_log)

        setupWebView()

        // ★ 解析 URI：支持两种来源
        val uri = resolveUri()
        log("收到 URI：$uri")

        if (uri != null) {
            val route = parseRoute(uri)      // "product/detail"
            val params = parseParams(uri)    // {"id":"123","name":"蓝牙耳机"}
            tvRoute.text = "路由：$route\n参数：$params"
            log("路由解析：route=$route, params=$params")
            loadRNPage(route, params)
        } else {
            tvRoute.text = "未收到有效 URI"
            log("❌ 没有 URI，无法路由")
        }

        findViewById<android.widget.Button>(R.id.btn_rn_back).setOnClickListener { finish() }
    }

    // ─── URI 解析 ────────────────────────────────────────────

    /**
     * 解析 URI 来源：
     *   1. Intent data（DeepLink / 外部跳转）
     *   2. Intent extra "uri"（Native 内部跳转）
     */
    private fun resolveUri(): Uri? {
        // 方式A：DeepLink，URI 在 intent.data 里
        intent.data?.let { return it }

        // 方式B：Native 内部跳转，URI 在 extra 里
        val uriStr = intent.getStringExtra("uri")
        return if (uriStr != null) Uri.parse(uriStr) else null
    }

    /**
     * 从 URI 提取路由路径
     * myapp://product/detail?id=123  →  "product/detail"
     */
    private fun parseRoute(uri: Uri): String {
        // uri.host = "product", uri.path = "/detail"
        val host = uri.host ?: ""
        val path = uri.path?.removePrefix("/") ?: ""
        return if (path.isEmpty()) host else "$host/$path"
    }

    /**
     * 从 URI 提取 Query 参数
     * ?id=123&name=耳机  →  {"id":"123","name":"耳机"}
     */
    private fun parseParams(uri: Uri): Map<String, String> {
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { key ->
            params[key] = uri.getQueryParameter(key) ?: ""
        }
        return params
    }

    // ─── 加载 RN 页面 ────────────────────────────────────────

    /**
     * ★ 核心：把 route/params 注入 HTML，再加载 bundle
     *
     * 真实 RN 里：ReactRootView.startReactApplication(manager, "MyApp", initialProps)
     * 本 Demo：把路由信息写进 window.ROUTE_CONFIG，让 JS Router 读取
     */
    private fun loadRNPage(route: String, params: Map<String, String>) {
        val bundleJs = assets.open("router_bundle.js").bufferedReader().readText()

        // 把参数序列化成 JS 对象字面量
        val paramsJs = params.entries.joinToString(",") { (k, v) ->
            "\"$k\": \"${v.replace("\"", "\\\"")}\""
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script>
                    // ★ Native 把路由信息注入到 window.ROUTE_CONFIG
                    // JS Router 启动时读取这个配置，决定渲染哪个页面
                    window.ROUTE_CONFIG = {
                        route: "$route",
                        params: { $paramsJs }
                    };
                </script>
            </head>
            <body style="margin:0;padding:0;background:#F5F5F5">
                <div id="app-root"></div>
                <script>
                    $bundleJs
                    window.addEventListener('load', function() {
                        renderApp('app-root');
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }

    // ─── WebView + Bridge 配置 ────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage): Boolean {
                log("🌐 [JS] ${msg.message()}")
                return true
            }
        }
        webView.addJavascriptInterface(RNBridge(), "NativeBridge")
    }

    // ─── Native Bridge（JS 可调用的 Native 方法）────────────

    inner class RNBridge {

        @JavascriptInterface
        fun onPageReady(route: String) {
            runOnUiThread { log("✅ [Native←JS] 页面就绪：$route") }
        }

        @JavascriptInterface
        fun addToCart(productId: String, productName: String) {
            runOnUiThread {
                log("🛒 [Native←JS] addToCart: id=$productId, name=$productName")
                android.widget.Toast.makeText(
                    this@RNContainerActivity,
                    "已加入购物车：$productName",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        @JavascriptInterface
        fun getUserInfo(userId: String) {
            runOnUiThread {
                log("👤 [Native←JS] getUserInfo: $userId")
                val info = org.json.JSONObject(mapOf(
                    "name" to "王安杰", "team" to "海外增长", "userId" to userId
                ))
                // Native → JS 回调
                webView.evaluateJavascript("onUserInfoReceived($info);", null)
                log("👤 [Native→JS] onUserInfoReceived($info)")
            }
        }

        @JavascriptInterface
        fun goBack() {
            runOnUiThread {
                log("🔙 [Native←JS] goBack()")
                finish()
            }
        }
    }

    private fun log(msg: String) {
        Log.d("RNContainer", msg)
        runOnUiThread { tvLog.append("$msg\n") }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
