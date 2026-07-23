package com.example.krn

import android.os.Build
import android.widget.Toast
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import org.json.JSONObject

/**
 * ★★★ YodaBridgeModule：自定义 Native Module ★★★
 *
 * 这是 RN Bridge 的 Android 侧实现。JS 里调用：
 *   yoda.invoke('YodaBridge.showToast', { msg: 'hello' })
 * 会通过 RN Bridge 最终执行到这个类的 showToast() 方法。
 *
 * 和 KRN 的对应关系：
 *   KRN 里  yoda.invoke('live.showToast', params)
 *     → @yoda/bridge 包 → JSI → 快手内部 LiveBridgeModule.kt
 *   本 Demo  yoda.invoke('YodaBridge.showToast', params)
 *     → 我们的 yoda.ts → NativeModules.YodaBridge → YodaBridgeModule.kt（这里）
 *
 * 机制完全一样，只是模块名和具体逻辑不同。
 *
 * ReactContextBaseJavaModule 是所有 RN Native Module 的基类，
 * getName() 返回的字符串就是 JS 里 NativeModules['YodaBridge'] 的键名。
 */
class YodaBridgeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    // ★ 这个名字 = JS 里 NativeModules['YodaBridge'] 的键
    override fun getName(): String = "YodaBridge"

    /**
     * 方法1：showToast
     *
     * JS 调用：yoda.invoke('YodaBridge.showToast', { msg: '...' })
     *
     * @param paramsJson JS 传来的 JSON 字符串（我们的 invoke 兼容层序列化的）
     * @param successCallback 成功回调 → Promise.resolve
     * @param errorCallback   失败回调 → Promise.reject
     *
     * @ReactMethod 注解让 RN Bridge 把这个方法暴露给 JS，
     * 没有这个注解的方法 JS 看不到。
     */
    @ReactMethod
    fun showToast(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        try {
            val params = JSONObject(paramsJson)
            val msg = params.optString("msg", "Hello from Native!")

            // ★ RN Bridge 回调默认在 JS 线程，UI 操作必须切主线程
            reactContext.currentActivity?.runOnUiThread {
                Toast.makeText(reactContext, msg, Toast.LENGTH_SHORT).show()
            }

            // 回调成功（空 JSON 对象）
            successCallback.invoke("""{"result":1}""")
        } catch (e: Exception) {
            errorCallback.invoke("showToast failed: ${e.message}")
        }
    }

    /**
     * 方法2：getDeviceInfo（演示有返回值的 invoke）
     *
     * JS 调用：yoda.invoke('YodaBridge.getDeviceInfo', {})
     * 返回：{ model, os, appVersion }
     *
     * 对照 KRN：yoda.invoke('device.getInfo', {})
     *   → 快手 DeviceBridgeModule.kt 返回设备信息
     */
    @ReactMethod
    fun getDeviceInfo(paramsJson: String, successCallback: Callback, errorCallback: Callback) {
        try {
            val model = Build.MODEL               // 手机型号，如 "Pixel 7"
            val manufacturer = Build.MANUFACTURER // 厂商，如 "Google"
            val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            val appVersion = try {
                reactContext.packageManager
                    .getPackageInfo(reactContext.packageName, 0)
                    .versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }

            val result = JSONObject().apply {
                put("model", "$manufacturer $model")
                put("os", osVersion)
                put("appVersion", appVersion)
            }

            // ★ 把结果以 JSON 字符串形式传给 JS，JS 侧的 invoke 会 JSON.parse
            successCallback.invoke(result.toString())
        } catch (e: Exception) {
            errorCallback.invoke("getDeviceInfo failed: ${e.message}")
        }
    }
}
