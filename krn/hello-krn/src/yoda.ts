/**
 * ★★★ invoke 兼容层 ★★★
 *
 * 目标：模拟 KRN 里的 yoda.invoke('module.method', params) 语法
 *
 * 真实 KRN（快手内网）的调用链：
 *   yoda.invoke('live.showToast', { msg: 'hello' })
 *     → @yoda/bridge 包
 *     → JSI / TurboModules 跨越 JS/Native 边界
 *     → 快手定制的 Native Module（Kotlin/Java）
 *     → 执行 Native 逻辑，返回 Promise
 *
 * 本 Demo（开源 RN）的等价调用链：
 *   invoke('YodaBridge', 'showToast', { msg: 'hello' })
 *     → NativeModules.YodaBridge（标准 RN Bridge）
 *     → YodaBridgeModule.kt（我们自己写的 Native Module）
 *     → 执行 Kotlin 代码，callbackContext.resolve(result)
 *
 * 差异只在"API 封装层"，Bridge 机制完全相同。
 */

import { NativeModules } from 'react-native';

/**
 * invoke(moduleName, methodName, params) → Promise<any>
 *
 * 使用示例（对照 KRN 语法）：
 *   KRN:  yoda.invoke('live.showToast', { msg: 'hello' })
 *   本Demo: invoke('YodaBridge', 'showToast', { msg: 'hello' })
 *
 * KRN 里 'live.showToast' 是一个点分割的字符串，
 * 这里拆成两个参数更清晰，便于理解映射关系。
 */
export function invoke(
  moduleName: string,
  methodName: string,
  params: Record<string, unknown> = {}
): Promise<unknown> {
  const nativeModule = NativeModules[moduleName];

  if (!nativeModule) {
    // 模块不存在时：开发环境打印警告，不直接崩溃
    console.warn(`[invoke] Native module "${moduleName}" not found`);
    return Promise.reject(new Error(`Native module "${moduleName}" not found`));
  }

  const method = nativeModule[methodName];
  if (typeof method !== 'function') {
    console.warn(`[invoke] Method "${methodName}" not found on module "${moduleName}"`);
    return Promise.reject(
      new Error(`Method "${methodName}" not found on module "${moduleName}"`)
    );
  }

  // 标准 RN NativeModules 的方法是普通函数（不是 Promise），
  // 我们在 YodaBridgeModule.kt 里用 Callback 实现，这里包成 Promise
  return new Promise((resolve, reject) => {
    method(
      JSON.stringify(params), // 传 JSON 字符串，Native 侧 parse
      (result: string) => resolve(JSON.parse(result)),   // 成功回调
      (error: string) => reject(new Error(error))         // 失败回调
    );
  });
}

/**
 * yoda 对象：对照 KRN 的 yoda.invoke 语法
 *
 * KRN 用法：
 *   import yoda from '@/common/yoda';
 *   yoda.invoke('live.showToast', { msg: 'hello' })
 *
 * 这里我们用点分割字符串模拟同一语法：
 *   yoda.invoke('YodaBridge.showToast', { msg: 'hello' })
 */
export const yoda = {
  invoke(dotPath: string, params: Record<string, unknown> = {}): Promise<unknown> {
    const [moduleName, methodName] = dotPath.split('.');
    if (!methodName) {
      return Promise.reject(new Error(`Invalid invoke path: "${dotPath}"`));
    }
    return invoke(moduleName, methodName, params);
  },
};

export default yoda;
