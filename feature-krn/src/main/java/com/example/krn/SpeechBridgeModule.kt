package com.example.krn

import com.example.krn.speech.SpeechEvent
import com.example.krn.speech.SpeechRecognizerManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * ★★★ SpeechBridgeModule：语音识别的 RN Bridge ★★★
 *
 * JS 侧调用：
 *   NativeModules.Speech.start({ lang: 'zh-CN' })
 *   NativeModules.Speech.stop()
 *   NativeModules.Speech.cancel()
 *
 * JS 侧监听结果：
 *   DeviceEventEmitter.addListener('SpeechEvent', (data) => { ... })
 *
 * ★★★ 和 NativeBannerModule 的关键区别 ★★★
 *
 *   NativeBannerModule（问答式）：
 *     JS 调一次 → Native 执行 → successCallback.invoke() 返回一次 → 结束
 *
 *   SpeechBridgeModule（广播式）：
 *     JS 调 start() → promise.resolve() 立刻返回（只表示"命令已收到"）
 *     之后 Native 持续 emit 事件：READY → VOLUME × N → PARTIAL × N → FINAL
 *
 *   为什么必须这样：
 *     一次语音识别会产生 10~50 个事件，Promise 只能 resolve 一次，包不住。
 *
 *   ★ 参考代码（快手 SpeechBridgeModuleImpl）的 Bug：
 *     声明了 callback 参数但从不调用，导致 JS 侧 await 永久 pending。
 *     本实现明确区分：promise = 命令确认，emit = 结果通知。
 *
 * 职责边界：
 *   本类只做「翻译」——Kotlin SpeechEvent ↔ JS WritableMap
 *   不写任何录音/识别逻辑（那些在 SpeechRecognizerManager 里）
 */
class SpeechBridgeModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        /** JS 侧监听的事件名，必须和 speechService.ts 里保持一致 */
        private const val EVENT_NAME = "SpeechEvent"
    }

    /**
     * getName() 的返回值决定 JS 怎么调用：
     *   返回 "Speech" → JS 写 NativeModules.Speech.start()
     */
    override fun getName(): String = "Speech"

    /**
     * 识别引擎，懒加载
     * 用 ReactApplicationContext 而非 Activity——参考代码持有 FragmentActivity 会泄漏
     */
    private val manager by lazy { SpeechRecognizerManager(reactContext) }

    // ─── 对 JS 暴露的方法 ─────────────────────────────────────────────────────

    /**
     * 开始语音识别
     *
     * @param options 可选参数，支持 { lang: 'zh-CN' }
     * @param promise 立刻 resolve，仅表示"命令已下发"，不代表识别完成
     */
    @ReactMethod
    fun start(options: ReadableMap?, promise: Promise) {
        // ReadableMap 取值必须先 hasKey 判断，否则 key 不存在会抛异常
        val lang = if (options != null && options.hasKey("lang")) {
            options.getString("lang") ?: "zh-CN"
        } else {
            "zh-CN"
        }

        // SpeechRecognizer 强制主线程，而 @ReactMethod 默认跑在 Native Modules 线程
        UiThreadUtil.runOnUiThread {
            manager.start(lang) { event -> emitToJS(event) }
        }

        // ★ 立刻 resolve，不等识别结果
        promise.resolve(null)
    }

    /** 主动结束录音，仍会等最终结果（用户点"完成"） */
    @ReactMethod
    fun stop(promise: Promise) {
        UiThreadUtil.runOnUiThread { manager.stop() }
        promise.resolve(null)
    }

    /** 取消识别，丢弃结果（用户点"取消"或页面退出） */
    @ReactMethod
    fun cancel(promise: Promise) {
        UiThreadUtil.runOnUiThread { manager.cancel() }
        promise.resolve(null)
    }

    /**
     * ★ 这两个空方法必须有 ★
     *
     * RN 要求：如果一个 Module 通过 RCTDeviceEventEmitter 发事件，
     * 就必须提供 addListener / removeListeners，否则 JS 侧会打警告：
     *   "new NativeEventEmitter() was called with a non-null argument
     *    without the required addListener method"
     *
     * 实际的监听逻辑由 RN 框架内部处理，这里只是满足接口契约。
     */
    @ReactMethod
    fun addListener(eventName: String) = Unit

    @ReactMethod
    fun removeListeners(count: Int) = Unit

    /**
     * ★ 生命周期回调：RN 实例销毁时释放资源 ★
     *
     * 参考代码用 object 单例 + HashMap 存 Module，永不清理 → Activity 永久泄漏。
     * 本实现绑定 RN 实例生命周期，页面关闭时自动释放 SpeechRecognizer。
     */
    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        manager.destroy()
    }

    // ─── 事件翻译与发送 ───────────────────────────────────────────────────────

    /**
     * 把 Kotlin 的 SpeechEvent 翻译成 JS 能读的对象，然后发给 JS
     *
     * 数据类型对应：
     *   putString  → JS string
     *   putDouble  → JS number（JS 没有 Int/Float 区分，统一 double）
     *   putInt     → JS number
     *
     * ★ when 是穷举的（sealed class），少写一个分支编译不过
     */
    private fun emitToJS(event: SpeechEvent) {
        val map = Arguments.createMap()

        when (event) {
            is SpeechEvent.Ready -> {
                map.putString("status", "READY")
            }

            is SpeechEvent.SpeechStart -> {
                map.putString("status", "SPEECH_START")
            }

            is SpeechEvent.Volume -> {
                map.putString("status", "VOLUME")
                map.putDouble("volume", event.rms.toDouble())
            }

            is SpeechEvent.SpeechEnd -> {
                map.putString("status", "SPEECH_END")
            }

            is SpeechEvent.Partial -> {
                map.putString("status", "PARTIAL")
                map.putString("text", event.text)
            }

            is SpeechEvent.Final -> {
                map.putString("status", "FINAL")
                map.putString("text", event.text)
            }

            is SpeechEvent.Failure -> {
                map.putString("status", "ERROR")
                map.putInt("code", event.code)
                map.putString("message", event.message)
            }

            is SpeechEvent.Cancelled -> {
                map.putString("status", "CANCELLED")
            }
        }

        // RCTDeviceEventEmitter 是 RN 内置的事件总线
        // 类比 Android 的 LocalBroadcastManager.sendBroadcast()
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(EVENT_NAME, map)
    }
}
