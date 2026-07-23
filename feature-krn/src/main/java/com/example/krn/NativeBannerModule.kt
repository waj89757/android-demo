package com.example.krn

import android.animation.ValueAnimator
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import com.example.core.BannerHost
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

/**
 * NativeBannerModule：RN 控制 Native Banner 的 Bridge
 *
 * JS 侧调用：
 *   yoda.invoke('NativeBanner.setHeight', { height: 160 })
 *   yoda.invoke('NativeBanner.setColor',  { color: '#E53935' })
 *   yoda.invoke('NativeBanner.setTitle',  { title: '🔥 新标题' })
 *   yoda.invoke('NativeBanner.reset',     {})
 *
 * 解耦说明：
 *   通过 BannerHost 接口（定义在 core 模块）操作宿主 Activity 的 View
 *   feature-krn 不直接依赖 app 模块，避免循环依赖
 */
class NativeBannerModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "NativeBanner"

    // ─── 改变高度（带动画）────────────────────────────────────────────────────

    @ReactMethod
    fun setHeight(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        try {
            val json = org.json.JSONObject(paramsJson)
            val targetDp = json.optInt("height", 80)

            val host = ActivityHolder.host
                ?: return errorCallback.invoke("BannerHost not available")

            val banner = host.findViewByResId(
                reactContext.resources.getIdentifier("native_banner", "id", reactContext.packageName)
            ) as? ViewGroup ?: return errorCallback.invoke("native_banner view not found")

            val targetPx = dpToPx(targetDp.toFloat())

            host.runOnMainThread {
                val animator = ValueAnimator.ofInt(banner.layoutParams.height, targetPx)
                animator.duration = 400
                animator.addUpdateListener { anim ->
                    val lp = banner.layoutParams
                    lp.height = anim.animatedValue as Int
                    banner.layoutParams = lp
                }
                animator.start()
                successCallback.invoke("""{"result":"height changed to ${targetDp}dp"}""")
            }
        } catch (e: Exception) {
            errorCallback.invoke("setHeight error: ${e.message}")
        }
    }

    // ─── 改变背景色────────────────────────────────────────────────────────────

    @ReactMethod
    fun setColor(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        try {
            val json = org.json.JSONObject(paramsJson)
            val colorStr = json.optString("color", "#1565C0")

            val host = ActivityHolder.host
                ?: return errorCallback.invoke("BannerHost not available")

            val bannerId = reactContext.resources.getIdentifier(
                "native_banner", "id", reactContext.packageName)
            val banner = host.findViewByResId(bannerId) as? ViewGroup
                ?: return errorCallback.invoke("native_banner view not found")

            host.runOnMainThread {
                banner.setBackgroundColor(Color.parseColor(colorStr))
                successCallback.invoke("""{"result":"color changed to $colorStr"}""")
            }
        } catch (e: Exception) {
            errorCallback.invoke("setColor error: ${e.message}")
        }
    }

    // ─── 改变标题文字 ─────────────────────────────────────────────────────────

    @ReactMethod
    fun setTitle(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        try {
            val json = org.json.JSONObject(paramsJson)
            val title = json.optString("title", "🎯 Native Banner")
            val subtitle = json.optString("subtitle", "")

            val host = ActivityHolder.host
                ?: return errorCallback.invoke("BannerHost not available")

            host.runOnMainThread {
                val titleId = reactContext.resources.getIdentifier(
                    "tv_banner_title", "id", reactContext.packageName)
                val subtitleId = reactContext.resources.getIdentifier(
                    "tv_banner_subtitle", "id", reactContext.packageName)
                (host.findViewByResId(titleId) as? TextView)?.text = title
                if (subtitle.isNotEmpty()) {
                    (host.findViewByResId(subtitleId) as? TextView)?.text = subtitle
                }
                successCallback.invoke("""{"result":"title updated"}""")
            }
        } catch (e: Exception) {
            errorCallback.invoke("setTitle error: ${e.message}")
        }
    }

    // ─── 恢复默认 ─────────────────────────────────────────────────────────────

    @ReactMethod
    fun reset(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        val host = ActivityHolder.host
            ?: return errorCallback.invoke("BannerHost not available")

        val bannerId = reactContext.resources.getIdentifier(
            "native_banner", "id", reactContext.packageName)
        val banner = host.findViewByResId(bannerId) as? ViewGroup
            ?: return errorCallback.invoke("native_banner view not found")

        host.runOnMainThread {
            val targetPx = dpToPx(80f)
            val animator = ValueAnimator.ofInt(banner.layoutParams.height, targetPx)
            animator.duration = 400
            animator.addUpdateListener { anim ->
                val lp = banner.layoutParams
                lp.height = anim.animatedValue as Int
                banner.layoutParams = lp
            }
            animator.start()

            banner.setBackgroundColor(Color.parseColor("#1565C0"))

            val titleId = reactContext.resources.getIdentifier(
                "tv_banner_title", "id", reactContext.packageName)
            val subtitleId = reactContext.resources.getIdentifier(
                "tv_banner_subtitle", "id", reactContext.packageName)
            (host.findViewByResId(titleId) as? TextView)?.text = "🎯 Native Banner"
            (host.findViewByResId(subtitleId) as? TextView)?.text = "等待 RN 控制..."

            successCallback.invoke("""{"result":"reset done"}""")
        }
    }

    // ─── 工具函数 ─────────────────────────────────────────────────────────────

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            reactContext.resources.displayMetrics
        ).toInt()
    }
}
