package com.example.krn

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * ★★★ YodaBridgePackage：把自定义 Module 注册到 RN ★★★
 *
 * ReactPackage 是 RN 的"模块包"接口。
 * 你每写一个 NativeModule，都要通过一个 Package 注册进来。
 *
 * 注册路径：
 *   YodaBridgePackage → createNativeModules() → [YodaBridgeModule]
 *       ↓
 *   MainApplication → ReactNativeHost.getPackages() → [YodaBridgePackage]
 *       ↓
 *   RN 运行时初始化时注册所有 Module
 *       ↓
 *   JS 里 NativeModules.YodaBridge 就能找到了
 */
class YodaBridgePackage : ReactPackage {

    // ★ 返回这个 Package 包含的所有 NativeModule
    override fun createNativeModules(
        reactContext: ReactApplicationContext
    ): List<NativeModule> = listOf(YodaBridgeModule(reactContext))

    // ★ 返回自定义 ViewManager（我们没有自定义原生 View，返回空列表）
    override fun createViewManagers(
        reactContext: ReactApplicationContext
    ): List<ViewManager<*, *>> = emptyList()
}
