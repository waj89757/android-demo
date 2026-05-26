package com.example.demo01

import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * ★★★ BundleManager：模拟 RN 热更新的 Bundle 管理器 ★★★
 *
 * 真实流程：
 *   1. App 启动 → 检查服务端是否有新 bundle 版本
 *   2. 有新版本 → 后台下载（本 Demo 模拟从 assets 复制）
 *   3. 校验 md5
 *   4. 写入私有目录
 *   5. 重启 RN 引擎时加载新 bundle
 *
 * 加载优先级：
 *   私有目录里的 bundle（热更新下发）> assets 里的 bundle（APK 内置）
 */
object BundleManager {

    private const val TAG = "BundleManager"

    // bundle 存储目录（模拟真实 RN 的 bundle 存储路径）
    private const val BUNDLE_DIR = "rn_bundles"
    private const val BUNDLE_FILE = "index.android.bundle"
    private const val VERSION_FILE = "bundle_version.txt"

    // 模拟服务端配置（真实场景：从 API 拉取）
    private val SERVER_CONFIG = mapOf(
        "latestVersion" to "2.0.0",
        "bundleAsset"   to "bundle_v2.js",   // 本 Demo 用 assets 模拟 CDN 下载
        "md5"           to ""                 // 真实场景要填 md5
    )

    // ===== 对外 API =====

    /**
     * 获取当前应该加载的 bundle 内容
     *
     * 优先级：私有目录（热更新） > assets（APK 内置）
     */
    fun getActiveBundleContent(context: Context): Pair<String, String> {
        val hotUpdateBundle = getHotUpdateBundleFile(context)

        return if (hotUpdateBundle.exists()) {
            val version = getInstalledVersion(context)
            log("✅ 加载热更新 bundle（v$version）：${hotUpdateBundle.absolutePath}")
            Pair(hotUpdateBundle.readText(), "热更新 v$version（来自私有目录）")
        } else {
            log("📦 加载 APK 内置 bundle（v1.0.0）：assets/bundle_v1.js")
            val content = context.assets.open("bundle_v1.js").bufferedReader().readText()
            Pair(content, "APK 内置 v1.0.0（来自 assets）")
        }
    }

    /**
     * 检查是否有新版本可用
     * 真实场景：请求 /api/bundle/check?currentVersion=xxx
     */
    fun checkUpdate(context: Context): UpdateCheckResult {
        val current = getInstalledVersion(context) ?: "1.0.0"  // 没热更新过，视为 v1
        val latest = SERVER_CONFIG["latestVersion"]!!

        log("检查更新：当前=$current，服务端最新=$latest")

        return if (current != latest) {
            UpdateCheckResult(
                hasUpdate = true,
                currentVersion = current,
                latestVersion = latest,
                downloadUrl = "https://cdn.example.com/bundles/$latest/index.android.bundle"
            )
        } else {
            UpdateCheckResult(hasUpdate = false, currentVersion = current, latestVersion = latest)
        }
    }

    /**
     * 执行热更新：下载并安装新 bundle
     * 真实场景：下载真实 CDN 上的 .bundle 文件
     * 本 Demo：从 assets 里读 v2 bundle 模拟下载
     */
    fun downloadAndInstall(
        context: Context,
        onProgress: (String) -> Unit,
        onSuccess: (String) -> Unit,
        onFail: (String) -> Unit
    ) {
        Thread {
            try {
                onProgress("开始下载 bundle v2.0.0...")
                Thread.sleep(800)  // 模拟网络下载延迟

                onProgress("下载完成，校验 md5...")
                Thread.sleep(300)

                // 从 assets 读取 v2 bundle（模拟从 CDN 下载）
                val newContent = context.assets.open("bundle_v2.js")
                    .bufferedReader().readText()

                // 模拟 md5 校验
                val md5 = md5(newContent)
                onProgress("md5 校验通过：${md5.take(16)}...")
                Thread.sleep(200)

                // 写入私有目录
                onProgress("写入私有目录...")
                saveBundleToPrivateDir(context, newContent, "2.0.0")
                Thread.sleep(200)

                log("✅ 热更新安装完成，版本：2.0.0，路径：${getHotUpdateBundleFile(context).absolutePath}")
                onSuccess("热更新安装成功！重启 RN 引擎后生效（本 Demo 重新加载 WebView）")

            } catch (e: Exception) {
                log("❌ 热更新失败：${e.message}")
                onFail("热更新失败：${e.message}")
            }
        }.start()
    }

    /**
     * 回滚：删除热更新 bundle，恢复使用 APK 内置 bundle
     */
    fun rollback(context: Context): Boolean {
        val bundleFile = getHotUpdateBundleFile(context)
        val versionFile = File(context.filesDir, "$BUNDLE_DIR/$VERSION_FILE")
        val deleted = bundleFile.delete() && versionFile.delete()
        log(if (deleted) "✅ 回滚成功，将使用 APK 内置 bundle v1.0.0" else "❌ 回滚失败")
        return deleted
    }

    fun getInstalledVersion(context: Context): String? {
        val versionFile = File(context.filesDir, "$BUNDLE_DIR/$VERSION_FILE")
        return if (versionFile.exists()) versionFile.readText().trim() else null
    }

    fun getBundleDirPath(context: Context): String =
        File(context.filesDir, BUNDLE_DIR).absolutePath

    // ===== 私有方法 =====

    private fun getHotUpdateBundleFile(context: Context): File =
        File(context.filesDir, "$BUNDLE_DIR/$BUNDLE_FILE")

    private fun saveBundleToPrivateDir(context: Context, content: String, version: String) {
        val dir = File(context.filesDir, BUNDLE_DIR)
        dir.mkdirs()
        // 写 bundle 文件
        File(dir, BUNDLE_FILE).writeText(content)
        // 写版本号文件
        File(dir, VERSION_FILE).writeText(version)
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun log(msg: String) = Log.d(TAG, msg)

    // ===== 数据类 =====

    data class UpdateCheckResult(
        val hasUpdate: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val downloadUrl: String = ""
    )
}
