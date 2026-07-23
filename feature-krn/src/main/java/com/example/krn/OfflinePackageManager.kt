package com.example.krn

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream

/**
 * ★★★ OfflinePackageManager ★★★
 *
 * 离线包管理器，真实流程：
 *   1. 从 assets（或网络）读取 zip 包
 *   2. 解压到 app 私有目录
 *   3. 解析 manifest.json，建立 URL → 本地路径 的映射表
 *   4. WebView 的 shouldInterceptRequest 查这个映射表
 */
object OfflinePackageManager {

    private const val TAG = "OfflinePkg"

    // URL → 本地文件绝对路径 的映射表（从 manifest.json 加载）
    private val urlMap = mutableMapOf<String, String>()

    // 解压目录
    private lateinit var unzipDir: File

    /**
     * 第一步：初始化
     *   - 从 assets 解压 zip 到私有目录（模拟从网络下载后解压）
     *   - 读 manifest.json，建立 URL 映射表
     */
    fun init(context: Context, zipAssetName: String = "offline_v1.zip") {
        unzipDir = File(context.filesDir, "offline/activity_v1")

        // 如果已经解压过，直接加载 manifest（跳过解压）
        val manifest = File(unzipDir, "manifest.json")
        if (!manifest.exists()) {
            Log.d(TAG, "解压 $zipAssetName → ${unzipDir.absolutePath}")
            unzipFromAssets(context, zipAssetName, unzipDir)
        } else {
            Log.d(TAG, "已有解压包，跳过解压")
        }

        // 加载 manifest.json 建立映射表
        loadManifest(manifest)
    }

    /**
     * 强制重新解压（模拟"收到新版本离线包，覆盖旧版本"）
     */
    fun forceReinstall(context: Context, zipAssetName: String = "offline_v1.zip") {
        unzipDir.deleteRecursively()
        urlMap.clear()
        init(context, zipAssetName)
        Log.d(TAG, "重新安装完成，映射表：$urlMap")
    }

    /**
     * shouldInterceptRequest 里调这个：
     * 给定 URL，返回对应的本地文件（没有则返回 null）
     */
    fun getLocalFile(url: String): File? {
        val localPath = urlMap[url] ?: return null
        val file = File(localPath)
        return if (file.exists()) file else null
    }

    fun getUrlMap(): Map<String, String> = urlMap.toMap()

    // ===== 私有方法 =====

    /**
     * 从 assets 解压 zip 到目标目录
     * 真实项目里这里换成"从下载目录解压"
     */
    private fun unzipFromAssets(context: Context, assetName: String, destDir: File) {
        destDir.mkdirs()
        context.assets.open(assetName).use { assetStream ->
            ZipInputStream(assetStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val outFile = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { out ->
                            zip.copyTo(out)
                        }
                        Log.d(TAG, "解压: ${entry.name} → ${outFile.absolutePath}")
                    }
                    entry = zip.nextEntry
                }
            }
        }
    }

    /**
     * 读 manifest.json，填充 urlMap
     *
     * manifest.json 格式：
     * {
     *   "package": "activity_v1",
     *   "version": "1.0.0",
     *   "files": {
     *     "https://m.example.com/activity/v1/index.html": "index.html",
     *     "https://m.example.com/activity/v1/static/main.css": "static/main.css"
     *   }
     * }
     */
    private fun loadManifest(manifestFile: File) {
        if (!manifestFile.exists()) {
            Log.e(TAG, "manifest.json 不存在: ${manifestFile.absolutePath}")
            return
        }

        val json = JSONObject(manifestFile.readText())
        val files = json.getJSONObject("files")
        files.keys().forEach { url ->
            val relativePath = files.getString(url)
            val absolutePath = File(unzipDir, relativePath).absolutePath
            urlMap[url] = absolutePath
            Log.d(TAG, "映射: $url → $absolutePath")
        }
        Log.d(TAG, "manifest 加载完成，共 ${urlMap.size} 条映射")
    }
}
