package com.example.krn.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ★★★ SpeechRecognizerManager：语音识别引擎封装 ★★★
 *
 * 设计原则：这个类完全不知道 React Native 的存在。
 *   输入：start(lang) { event -> ... } 一个回调
 *   输出：SpeechEvent 事件流
 *   → 纯 Native 页面（如 WanasActivity）也能直接 new 出来用
 *
 * 对照参考代码（快手电商 AudioRecordModule）的差异：
 *   参考代码                          本实现
 *   ──────────────────────────────    ────────────────────────────────
 *   Arya SDK（IMessageAudioRecord）   Android 系统 SpeechRecognizer
 *   只录音，产出 .aac 文件路径         直接产出识别后的文字
 *   RxJava2 Observable/Completable    Kotlin Coroutines
 *   自己实现静音超时（2秒）             系统自带（onEndOfSpeech 自动触发）
 *   自己实现无输入超时（5秒）           保留，本类实现
 *   持有 FragmentActivity（会泄漏）    只持有 ApplicationContext
 *
 * ★ 线程约束：SpeechRecognizer 的所有方法必须在主线程调用，回调也在主线程。
 */
class SpeechRecognizerManager(private val context: Context) {

    companion object {
        private const val TAG = "SpeechRecognizerManager"

        /** 无输入超时：启动后 N 毫秒未检测到语音就取消（借鉴参考代码 NO_INPUT_TIMEOUT_MS） */
        private const val NO_INPUT_TIMEOUT_MS = 5000L

        /** 音量事件节流：最小间隔（onRmsChanged 每 100ms 触发一次，太密集了） */
        private const val VOLUME_THROTTLE_MS = 200L
    }

    // ─── 内部状态 ─────────────────────────────────────────────────────────────

    private var recognizer: SpeechRecognizer? = null
    private var onEvent: ((SpeechEvent) -> Unit)? = null

    /** 主线程作用域，用于超时计时器 */
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    /** 无输入超时的计时任务 */
    private var noInputJob: Job? = null

    /** 本次识别是否已检测到语音（用于判断无输入超时是否该触发） */
    private var hasDetectedSpeech = false

    /** 是否正在识别中（防止快速连点导致 ERROR_RECOGNIZER_BUSY） */
    private var isListening = false

    /** 上次发出 Volume 事件的时间戳，用于节流 */
    private var lastVolumeEmitTime = 0L

    // ─── 对外 API ─────────────────────────────────────────────────────────────

    /**
     * 开始语音识别
     *
     * @param langTag 语言标签，如 "zh-CN"（中文普通话）/ "en-US"（美式英语）
     * @param onEvent 事件回调，会在主线程被调用多次
     */
    fun start(langTag: String = "zh-CN", onEvent: (SpeechEvent) -> Unit) {
        // ① 线程检查：SpeechRecognizer 强制要求主线程
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "start() must be called on main thread")
            onEvent(SpeechEvent.Failure(SpeechEvent.ERR_NOT_AVAILABLE, "内部错误：非主线程调用"))
            return
        }

        // ② 防重入：正在识别中就直接返回，不要重复 startListening
        //    参考代码没有这层保护，快速连点会拿到 ERROR_RECOGNIZER_BUSY
        if (isListening) {
            Log.w(TAG, "already listening, ignore")
            onEvent(SpeechEvent.Failure(SpeechEvent.ERR_BUSY, "正在识别中，请稍候"))
            return
        }

        // ③ 设备能力检查
        //    ★ Android 11+ 必须在 Manifest 里声明 <queries> 才能查到识别服务，
        //      否则这里永远返回 false
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "recognition not available on this device")
            onEvent(
                SpeechEvent.Failure(
                    SpeechEvent.ERR_NOT_AVAILABLE,
                    "设备不支持语音识别（模拟器或缺少 Google 服务）"
                )
            )
            return
        }

        // ④ 权限兜底检查
        //    JS 侧已用 PermissionsAndroid 申请过，这里是防御性判断
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            onEvent(SpeechEvent.Failure(SpeechEvent.ERR_NO_PERMISSION, "缺少录音权限"))
            return
        }

        this.onEvent = onEvent

        // ⑤ 重置本次识别的状态
        hasDetectedSpeech = false
        lastVolumeEmitTime = 0L

        // ⑥ 创建（或复用）recognizer 实例
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
        }

        // ⑦ 组装 Intent 并启动
        isListening = true
        recognizer?.startListening(buildIntent(langTag))

        // ⑧ 启动无输入超时计时
        startNoInputTimeout()

        Log.i(TAG, "startListening, lang=$langTag")
    }

    /**
     * 主动结束录音，但仍会等系统返回最终识别结果
     * 用户点击"完成"按钮时调用
     */
    fun stop() {
        Log.i(TAG, "stop()")
        cancelNoInputTimeout()
        recognizer?.stopListening()
        // 注意：不设 isListening = false，因为 onResults 还没回来
    }

    /**
     * 取消本次识别，丢弃结果
     * 用户点击"取消"或页面退出时调用
     */
    fun cancel() {
        Log.i(TAG, "cancel()")
        cancelNoInputTimeout()
        recognizer?.cancel()
        isListening = false
        onEvent?.invoke(SpeechEvent.Cancelled)
    }

    /**
     * 释放资源
     * ★ 必须调用，否则 SpeechRecognizer 会泄漏
     *   参考代码用 object 单例 + HashMap，永不清理，Activity 永久泄漏（Bug）
     */
    fun destroy() {
        Log.i(TAG, "destroy()")
        cancelNoInputTimeout()
        scope.coroutineContext[Job]?.cancel()
        recognizer?.destroy()
        recognizer = null
        onEvent = null
        isListening = false
    }

    // ─── Intent 构建 ──────────────────────────────────────────────────────────

    private fun buildIntent(langTag: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // FREE_FORM = 自由语音（适合长句），另一个选项是 WEB_SEARCH（适合短关键词）
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)

            // ★★★ 关键：开启实时部分结果 ★★★
            // 不加这一行，onPartialResults 不会被调用，就没有"边说边显示"的效果
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // 只要置信度最高的一条结果
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            // 某些 ROM 要求带上调用方包名，否则拒绝服务
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

    // ─── RecognitionListener：系统的 9 个回调 ─────────────────────────────────

    private val listener = object : RecognitionListener {

        /** ① 引擎准备好了，用户可以开始说话 */
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
            onEvent?.invoke(SpeechEvent.Ready)
        }

        /** ② 检测到用户开始说话 → 取消无输入超时 */
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
            hasDetectedSpeech = true
            cancelNoInputTimeout()
            onEvent?.invoke(SpeechEvent.SpeechStart)
        }

        /**
         * ③ 音量变化，约每 100ms 触发一次
         *    做节流处理：200ms 最多发一次，避免 Bridge 过载
         */
        override fun onRmsChanged(rmsdB: Float) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastVolumeEmitTime < VOLUME_THROTTLE_MS) return
            lastVolumeEmitTime = now
            onEvent?.invoke(SpeechEvent.Volume(rmsdB))
        }

        /** ④ 原始音频数据，本方案不需要 */
        override fun onBufferReceived(buffer: ByteArray?) = Unit

        /**
         * ⑤ 系统检测到用户说完了（静音一段时间后自动触发）
         *    ★ 这就是参考代码里手写的 SILENCE_TIMEOUT_MS = 2000L 的功能，
         *      系统免费提供，不用自己实现
         */
        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            cancelNoInputTimeout()
            onEvent?.invoke(SpeechEvent.SpeechEnd)
        }

        /**
         * ⑥ 实时部分识别结果
         *    说话过程中连续触发，文字逐渐变长
         */
        override fun onPartialResults(partialResults: Bundle?) {
            val text = extractText(partialResults)
            if (text.isNotEmpty()) {
                Log.d(TAG, "onPartialResults: $text")
                onEvent?.invoke(SpeechEvent.Partial(text))
            }
        }

        /** ⑦ 最终识别结果 */
        override fun onResults(results: Bundle?) {
            isListening = false
            cancelNoInputTimeout()

            val text = extractText(results)
            Log.i(TAG, "onResults: $text")

            if (text.isEmpty()) {
                // 系统返回空结果，等同于"没听懂"
                onEvent?.invoke(
                    SpeechEvent.Failure(
                        SpeechRecognizer.ERROR_NO_MATCH,
                        "没听清，请再说一次"
                    )
                )
            } else {
                onEvent?.invoke(SpeechEvent.Final(text))
            }
        }

        /** ⑧ 出错 */
        override fun onError(error: Int) {
            isListening = false
            cancelNoInputTimeout()

            val msg = translateError(error)
            Log.e(TAG, "onError: code=$error, msg=$msg")
            onEvent?.invoke(SpeechEvent.Failure(error, msg))
        }

        /** ⑨ 保留接口，不用管 */
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    // ─── 工具方法 ─────────────────────────────────────────────────────────────

    /**
     * 从 Bundle 里取出识别文字
     * 系统返回的是一个候选列表（按置信度降序），取第一条
     */
    private fun extractText(bundle: Bundle?): String {
        val list = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        return list?.firstOrNull()?.trim() ?: ""
    }

    /**
     * 把系统错误码翻译成用户能看懂的中文
     * 直接返回给 RN 展示，避免 JS 侧再写一遍映射表
     */
    private fun translateError(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时，请检查网络"
        SpeechRecognizer.ERROR_NETWORK -> "网络异常，语音识别需要联网"
        SpeechRecognizer.ERROR_AUDIO -> "录音失败，请检查麦克风"
        SpeechRecognizer.ERROR_SERVER -> "识别服务异常"
        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有检测到语音"
        SpeechRecognizer.ERROR_NO_MATCH -> "没听清，请再说一次"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别引擎繁忙，请稍后重试"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "缺少录音权限"
        else -> "识别失败（错误码 $code）"
    }

    /**
     * 启动无输入超时：5 秒内没检测到语音就取消
     * 对照参考代码的 startNoInputTimeoutMonitoring()，用协程替代 RxJava
     */
    private fun startNoInputTimeout() {
        cancelNoInputTimeout()
        noInputJob = scope.launch {
            delay(NO_INPUT_TIMEOUT_MS)
            if (!hasDetectedSpeech) {
                Log.w(TAG, "no input timeout")
                recognizer?.cancel()
                isListening = false
                onEvent?.invoke(
                    SpeechEvent.Failure(
                        SpeechEvent.ERR_NO_INPUT,
                        "5 秒内未检测到语音，已自动取消"
                    )
                )
            }
        }
    }

    private fun cancelNoInputTimeout() {
        noInputJob?.cancel()
        noInputJob = null
    }
}
