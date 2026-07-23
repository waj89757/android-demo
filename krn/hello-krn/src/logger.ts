/**
 * ★★★ logger.ts — 简版埋点 SDK ★★★
 *
 * 对照真实 KRN 里的 logger（@kuaishou/logger 或类似内部 SDK）
 *
 * 真实 KRN 里的调用方：
 *   logger.sendShow({ action: 'CHATROOM_MINIP', params })
 *   logger.sendClick({ action: 'CHATROOM_MINIP_FOLLOW', params })
 *
 * 原理：
 *   logger 把埋点事件打包成标准格式，
 *   通过 yoda.invoke('track.sendShow', {...}) 传给 Native，
 *   Native 负责上报到数据平台（这里用 console.log + Toast 模拟）。
 *
 * 本 Demo 实现：
 *   用 yoda.invoke('YodaBridge.track', { type, action, params }) 发到 Android，
 *   Android 端用 Toast 显示埋点内容（方便看到效果）。
 */

import yoda from './yoda';

export interface TrackParams {
  [key: string]: string | number | boolean | undefined;
}

export interface SendShowOptions {
  action: string;
  params?: TrackParams;
}

export interface SendClickOptions {
  action: string;
  params?: TrackParams;
}

const logger = {
  /**
   * 页面曝光埋点（Show = 页面/组件进入可见区域）
   * 对照：logger.sendShow({ action: 'CHATROOM_MINIP', params })
   */
  sendShow(options: SendShowOptions): void {
    const payload = {
      type: 'SHOW',
      action: options.action,
      params: options.params ?? {},
      ts: Date.now(),
    };
    console.log('[Track][SHOW]', JSON.stringify(payload));
    // 通过 Bridge 发给 Native，Native 用 Toast 展示（方便调试）
    yoda
      .invoke('YodaBridge.track', payload)
      .catch(e => console.warn('[Track] sendShow failed:', e));
  },

  /**
   * 点击埋点
   * 对照：logger.sendClick({ action: 'CHATROOM_MINIP_FOLLOW_BTN', params })
   */
  sendClick(options: SendClickOptions): void {
    const payload = {
      type: 'CLICK',
      action: options.action,
      params: options.params ?? {},
      ts: Date.now(),
    };
    console.log('[Track][CLICK]', JSON.stringify(payload));
    yoda
      .invoke('YodaBridge.track', payload)
      .catch(e => console.warn('[Track] sendClick failed:', e));
  },
};

export default logger;
