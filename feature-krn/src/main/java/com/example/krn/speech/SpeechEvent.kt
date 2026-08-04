package com.example.krn.speech

/**
 * ★★★ SpeechEvent：语音识别的事件定义 ★★★
 *
 * 为什么用 sealed class 而不是 Int 常量：
 *   参考代码（快手电商）用的是：
 *     const val COMPLETE = 2
 *     listener?.onRecordComplete(filePath)
 *   问题：状态和数据分离，编译器不检查，容易传错类型。
 *
 *   sealed class 的好处：
 *     1. when 表达式必须穷举所有分支，漏一个编译不过
 *     2. 每种事件带的数据类型由编译器保证
 *     3. IDE 自动补全知道 Partial 有 text 字段、Volume 有 rms 字段
 *
 * 事件时序（一次完整的语音识别）：
 *   Ready → SpeechStart → Volume × N → Partial × N → SpeechEnd → Final
 *
 * 异常路径：
 *   Ready → (5秒无声) → Failure(ERR_NO_INPUT)
 *   Ready → SpeechStart → Failure(ERROR_NO_MATCH)  // 说了但没听懂
 */
sealed class SpeechEvent {

    /** 引擎准备就绪，可以开始说话了（对应 onReadyForSpeech） */
    object Ready : SpeechEvent()

    /** 检测到用户开始说话（对应 onBeginningOfSpeech） */
    object SpeechStart : SpeechEvent()

    /**
     * 音量变化，用于驱动 RN 侧的声波动画
     * @param rms 音量值，系统返回范围大约 -2f ~ 10f
     */
    data class Volume(val rms: Float) : SpeechEvent()

    /** 检测到用户说完了，正在做最终识别（对应 onEndOfSpeech） */
    object SpeechEnd : SpeechEvent()

    /**
     * 实时部分识别结果（对应 onPartialResults）
     * 说话过程中会连续触发多次，文字逐渐变长：
     *   "今天" → "今天天" → "今天天气" → "今天天气真"
     *
     * ★ 这是系统 SpeechRecognizer 的核心优势。
     *   参考代码那套"录 aac 文件 → 上传服务端"完全没有这个能力。
     */
    data class Partial(val text: String) : SpeechEvent()

    /** 最终识别结果（对应 onResults） */
    data class Final(val text: String) : SpeechEvent()

    /**
     * 失败
     * @param code  错误码（系统 SpeechRecognizer.ERROR_* 或本文件自定义的 ERR_*）
     * @param message 已翻译成中文的错误描述，可直接展示给用户
     */
    data class Failure(val code: Int, val message: String) : SpeechEvent()

    /** 用户主动取消 */
    object Cancelled : SpeechEvent()

    companion object {
        // ─── 自定义错误码（从 100 开始，避开系统的 1~9）───────────────────────
        /** 设备不支持语音识别（无 Google 服务 / 模拟器） */
        const val ERR_NOT_AVAILABLE = 100

        /** 缺少 RECORD_AUDIO 权限 */
        const val ERR_NO_PERMISSION = 101

        /** 启动后 5 秒未检测到任何语音（借鉴参考代码的 NO_INPUT_TIMEOUT_MS） */
        const val ERR_NO_INPUT = 102

        /** 识别引擎正在工作中，忽略本次请求 */
        const val ERR_BUSY = 103
    }
}
