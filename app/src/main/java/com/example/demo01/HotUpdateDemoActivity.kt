package com.example.demo01

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ 热更新 Demo ★★★
 *
 * 完整模拟 RN 热更新流程：
 *
 * Step 1. App 启动 → BundleManager 决定加载哪个 bundle
 *         优先级：私有目录（热更新） > assets（APK 内置）
 *
 * Step 2. 检查更新 → 模拟向服务端请求最新版本号
 *
 * Step 3. 下载安装 → 下载新 bundle 到私有目录，校验 md5
 *
 * Step 4. 重载 → 重新加载 WebView（对应 RN 重启 JS 引擎）
 *
 * Step 5. 回滚 → 删除热更新 bundle，恢复 APK 内置版本
 */
class HotUpdateDemoActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvLog: TextView
    private lateinit var tvBundleSource: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_update_demo)

        webView = findViewById(R.id.wv_bundle)
        tvLog = findViewById(R.id.tv_hot_log)
        tvBundleSource = findViewById(R.id.tv_bundle_source)

        setupWebView()

        // ★ Step 1：启动时加载当前 bundle（可能是 v1 或热更新后的 v2）
        log("App 启动，加载 bundle...")
        loadActiveBundle()

        // Step 2：检查更新
        findViewById<Button>(R.id.btn_check_update).setOnClickListener {
            checkUpdate()
        }

        // Step 3：下载并安装新 bundle
        findViewById<Button>(R.id.btn_download).setOnClickListener {
            downloadAndInstall()
        }

        // Step 4：重新加载（模拟重启 RN 引擎）
        findViewById<Button>(R.id.btn_reload).setOnClickListener {
            log("\n--- 重载 bundle（模拟重启 RN 引擎）---")
            loadActiveBundle()
        }

        // Step 5：回滚
        findViewById<Button>(R.id.btn_rollback).setOnClickListener {
            log("\n--- 执行回滚 ---")
            val success = BundleManager.rollback(this)
            if (success) {
                log("✅ 回滚成功，重新加载 APK 内置 bundle...")
                loadActiveBundle()
            } else {
                log("❌ 回滚失败（可能还没热更新过）")
            }
        }

        findViewById<Button>(R.id.btn_back_hot).setOnClickListener { finish() }
    }

    // ===== 核心方法 =====

    /**
     * ★ 加载当前活跃的 bundle
     * BundleManager 决定用哪个版本（热更新 or APK 内置）
     */
    private fun loadActiveBundle() {
        val (bundleContent, bundleSource) = BundleManager.getActiveBundleContent(this)

        // 更新来源标签
        tvBundleSource.text = "当前 Bundle：$bundleSource"

        // 用 WebView 执行 bundle JS（模拟 RN JS 引擎执行 bundle）
        val html = buildHtmlWrapper(bundleContent)
        // ★ baseUrl 必须是 file:// 而不是 null
        // null 会导致 origin=about:blank，JavascriptInterface 注入失败
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)

        log("✅ Bundle 已加载：$bundleSource")
        log("存储路径：${BundleManager.getBundleDirPath(this)}")
    }

    private fun checkUpdate() {
        log("\n--- 检查更新 ---")
        val result = BundleManager.checkUpdate(this)
        if (result.hasUpdate) {
            log("🆕 发现新版本！")
            log("  当前版本：${result.currentVersion}")
            log("  最新版本：${result.latestVersion}")
            log("  下载地址：${result.downloadUrl}")
            log("  → 点「下载安装」执行热更新")
        } else {
            log("✅ 已是最新版本（${result.currentVersion}），无需更新")
        }
    }

    private fun downloadAndInstall() {
        log("\n--- 开始热更新 ---")
        // 禁用按钮防止重复点击
        findViewById<Button>(R.id.btn_download).isEnabled = false

        BundleManager.downloadAndInstall(
            context = this,
            onProgress = { msg ->
                runOnUiThread { log("⏳ $msg") }
            },
            onSuccess = { msg ->
                runOnUiThread {
                    log("✅ $msg")
                    log("→ 点「重载 Bundle」查看新版本效果")
                    findViewById<Button>(R.id.btn_download).isEnabled = true
                }
            },
            onFail = { msg ->
                runOnUiThread {
                    log("❌ $msg")
                    findViewById<Button>(R.id.btn_download).isEnabled = true
                }
            }
        )
    }

    // ===== WebView 配置 =====

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // ★ 捕获 JS 控制台日志，帮助调试
        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage): Boolean {
                runOnUiThread {
                    log("🌐 [JS Console] ${msg.message()} (${msg.sourceId()}:${msg.lineNumber()})")
                }
                return true
            }
        }

        // ★ BundleBridge：必须是具名内部类，匿名 object{} 反射找不到方法
        webView.addJavascriptInterface(NativeBridge(), "NativeBridge")
    }

    /**
     * ★ 具名内部类：JavascriptInterface 必须用具名类
     * 匿名 object{} 的方法 Android 反射机制找不到 → "non-injected object" 错误
     */
    inner class NativeBridge {

        @JavascriptInterface
        fun onBundleLoaded(version: String) {
            runOnUiThread { log("📱 [Native←JS] onBundleLoaded: v$version") }
        }

        @JavascriptInterface
        fun getDeviceInfo() {
            runOnUiThread {
                log("📱 [Native←JS] getDeviceInfo() 被调用")
                val info = mapOf(
                    "model"   to android.os.Build.MODEL,
                    "brand"   to android.os.Build.BRAND,
                    "android" to android.os.Build.VERSION.RELEASE,
                    "sdk"     to android.os.Build.VERSION.SDK_INT.toString(),
                    "screen"  to "${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}",
                    "density" to resources.displayMetrics.density.toString()
                )
                val json = org.json.JSONObject(info).toString()
                webView.evaluateJavascript("onDeviceInfoReceived($json);", null)
                log("📱 [Native→JS] evaluateJavascript: onDeviceInfoReceived()")
            }
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                log("🔔 [Native←JS] showToast('$message')")
                android.widget.Toast.makeText(
                    this@HotUpdateDemoActivity, message, android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        @JavascriptInterface
        fun trackEvent(eventName: String, paramsJson: String) {
            runOnUiThread {
                log("📊 [Native←JS] trackEvent: $eventName | params: $paramsJson")
            }
        }

        @JavascriptInterface
        fun openPage(pageName: String) {
            runOnUiThread {
                log("🚀 [Native←JS] openPage('$pageName')")
                android.widget.Toast.makeText(
                    this@HotUpdateDemoActivity,
                    "JS 调用 openPage: $pageName", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        @JavascriptInterface
        fun getUserInfo() {
            runOnUiThread {
                log("👤 [Native←JS] getUserInfo() 被调用")
                val userInfo = mapOf(
                    "userId" to "u_10086",
                    "name"   to "王安杰",
                    "role"   to "技术负责人",
                    "team"   to "海外增长"
                )
                val json = org.json.JSONObject(userInfo).toString()
                webView.evaluateJavascript("onUserInfoReceived($json);", null)
                log("👤 [Native→JS] evaluateJavascript: onUserInfoReceived()")
            }
        }
    }

    /**
     * 把 bundle JS 包装成完整 HTML，让 WebView 执行
     * 真实 RN 里：JS 引擎直接执行 bundle，不需要 HTML 包装
     */
    private fun buildHtmlWrapper(bundleJs: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:#f5f5f5">
            <div id="app-root"></div>
            <script>
                $bundleJs

                // ★ 延迟执行，确保 JavascriptInterface 注入完成
                // Bridge 注入是异步的，直接同步调用会报 non-injected 错误
                window.addEventListener('load', function() {
                    renderApp('app-root');
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    private fun log(msg: String) {
        android.util.Log.d("HotUpdateDemo", msg)   // ★ 同时输出到 Logcat
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
