/**
 * ★★★ speechService.ts — 语音识别的 JS 侧封装 ★★★
 *
 * 职责：把 Native 的调用细节包起来，组件里不用碰 NativeModules / DeviceEventEmitter
 *
 * 为什么要这一层：
 *   1. VoiceButton 不该知道「事件名叫 SpeechEvent」这种实现细节
 *   2. 事件名改了只改这一个文件
 *   3. 给 Native 传回的数据加上 TypeScript 类型，编译期就能查错
 *   4. 权限申请逻辑收在一处
 *
 * 对照 Android：这层相当于 Repository，组件相当于 Activity
 *
 * ★ 两种通道的分工（这是理解本功能的关键）：
 *
 *   命令通道（NativeModules.Speech.xxx）：JS → Native，一次性
 *     start / stop / cancel
 *     Promise 立刻 resolve，只表示「命令下发成功」
 *
 *   事件通道（DeviceEventEmitter）：Native → JS，持续多次
 *     READY → SPEECH_START → VOLUME × N → PARTIAL × N → SPEECH_END → FINAL
 *
 *   类比 Android：命令通道 = 方法调用，事件通道 = LiveData.observe
 */

import { NativeModules, DeviceEventEmitter, PermissionsAndroid } from 'react-native';

const { Speech } = NativeModules;

// ─── 类型定义 ──────────────────────────────────────────────────────────────

/**
 * 识别状态，和 Kotlin 侧 SpeechBridgeModule.emitToJS() 的 status 字段一一对应
 */
export type SpeechStatus =
  | 'READY'         // 引擎就绪，可以说话了
  | 'SPEECH_START'  // 检测到开始说话
  | 'VOLUME'        // 音量变化（驱动声波动画）
  | 'SPEECH_END'    // 说完了，正在做最终识别
  | 'PARTIAL'       // 实时部分结果（边说边出字）
  | 'FINAL'         // 最终结果
  | 'ERROR'         // 出错
  | 'CANCELLED';    // 已取消

export interface SpeechEventData {
  status: SpeechStatus;
  /** PARTIAL / FINAL 时携带识别出的文字 */
  text?: string;
  /** VOLUME 时携带音量值，系统返回范围约 -2 ~ 10 */
  volume?: number;
  /** ERROR 时携带错误码 */
  code?: number;
  /** ERROR 时携带已翻译成中文的错误描述，可直接展示 */
  message?: string;
}

/** 事件名，必须和 SpeechBridgeModule.kt 的 EVENT_NAME 保持一致 */
const EVENT_NAME = 'SpeechEvent';

// ─── Service ───────────────────────────────────────────────────────────────

export const speechService = {
  /**
   * 申请录音权限
   *
   * ★ 为什么权限在 JS 侧申请而不是 Native 侧：
   *   Native Module 不是 Activity，拿不到 onRequestPermissionsResult 回调。
   *   参考代码（快手）用 PermissionUtils + RxJava 包了一层解决这个问题，
   *   但 RN 已经内置了 PermissionsAndroid，一行搞定，没必要造轮子。
   */
  async requestPermission(): Promise<boolean> {
    try {
      const result = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
        {
          title: '需要麦克风权限',
          message: '用于将您的语音转换成文字',
          buttonPositive: '允许',
          buttonNegative: '拒绝',
        },
      );
      return result === PermissionsAndroid.RESULTS.GRANTED;
    } catch (e) {
      console.warn('[speechService] requestPermission failed:', e);
      return false;
    }
  },

  /**
   * 开始语音识别
   *
   * @param lang 语言标签，'zh-CN'（中文）/ 'en-US'（英文）
   * @returns false 表示权限被拒或 Native 模块不可用；true 表示已成功启动
   *
   * ★ 返回 true 不代表识别完成，只代表启动成功。
   *   识别结果通过 onEvent 回调持续推送。
   */
  async start(lang: string = 'zh-CN'): Promise<boolean> {
    if (!Speech) {
      console.warn('[speechService] NativeModules.Speech not found');
      return false;
    }

    const granted = await this.requestPermission();
    if (!granted) {
      return false;
    }

    try {
      await Speech.start({ lang });
      return true;
    } catch (e) {
      console.warn('[speechService] start failed:', e);
      return false;
    }
  },

  /** 主动结束录音，仍会等最终识别结果（用户点「完成」） */
  async stop(): Promise<void> {
    if (!Speech) return;
    try {
      await Speech.stop();
    } catch (e) {
      console.warn('[speechService] stop failed:', e);
    }
  },

  /** 取消识别，丢弃结果（用户点「取消」或页面退出） */
  async cancel(): Promise<void> {
    if (!Speech) return;
    try {
      await Speech.cancel();
    } catch (e) {
      console.warn('[speechService] cancel failed:', e);
    }
  },

  /**
   * 监听识别事件
   *
   * @param cb 每次 Native emit 一次就调一次
   * @returns 取消订阅函数，必须在 useEffect 的清理函数里调用
   *
   * ★ 为什么必须返回取消函数：
   *   不取消的话，组件卸载后监听还在，Native 继续 emit，
   *   回调里的 setState 会对已销毁组件调用 → RN 报内存泄漏警告。
   *   这和 Android 里 onDestroy 要 removeObserver 是同一个道理。
   *
   * 用法：
   *   useEffect(() => speechService.onEvent(handler), [handler]);
   *                    ↑ useEffect 会把返回值当清理函数
   */
  onEvent(cb: (data: SpeechEventData) => void): () => void {
    const subscription = DeviceEventEmitter.addListener(EVENT_NAME, cb);
    return () => subscription.remove();
  },
};

export default speechService;
