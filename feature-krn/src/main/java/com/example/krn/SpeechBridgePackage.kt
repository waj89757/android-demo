package com.example.krn

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * SpeechBridgePackage：把 SpeechBridgeModule 注册到 RN
 *
 * Package 和 Module 的关系，类比 Android：
 *   Module  ≈ 一个 Service 类
 *   Package ≈ AndroidManifest.xml 里的 <service> 注册项
 *
 * 一个 Package 可以装多个 Module（第三方库通常这么做）。
 * 最后要在 MainApplication.getPackages() 里把这个 Package 加进去，
 * 相当于把 <service> 写进 AndroidManifest。
 */
class SpeechBridgePackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
        listOf(SpeechBridgeModule(reactContext))

    /** 本 Package 不提供自定义 UI 组件，返回空 */
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        emptyList()
}
