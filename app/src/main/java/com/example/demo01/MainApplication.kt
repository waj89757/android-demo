package com.example.demo01

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader

/**
 * ★★★ MainApplication：RN 宿主应用入口 ★★★
 *
 * 每个集成了 React Native 的 App 都需要一个 Application 类实现 ReactApplication。
 * 它负责：
 *   1. 初始化 SoLoader（加载 RN 的 .so 原生库：libreactnative.so 等）
 *   2. 提供 ReactNativeHost（RN 运行时的配置中心）
 *   3. 告诉 RN：bundle 在哪里、用什么 JS 引擎、有哪些自定义 Package
 *
 * 对照 KRN：
 *   快手 App 也有自己的 Application 继承自某个 KRN 基类，
 *   内部同样实现了 ReactNativeHost，注册了大量快手内部的 Package（桥模块）
 *
 * ★ 注意：需要在 AndroidManifest.xml 的 <application> 标签加上
 *   android:name=".MainApplication"，否则这个类不会被 Android 加载。
 */
class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {

            // ★ 注册所有 ReactPackage
            //   MainReactPackage() = RN 核心内置模块（Timing、AppState、UIManager 等）
            //   YodaBridgePackage() = 我们自定义的 Bridge 模块
            //
            //   注：正式项目里用 PackageList(this).packages 自动链接第三方包，
            //   那需要 RN Gradle 插件（autolinking）。本 Demo 无第三方原生包，
            //   手动列出即可，更直观。
            override fun getPackages(): List<ReactPackage> =
                listOf(MainReactPackage(), YodaBridgePackage())

            // ★ bundle 文件名
            //   在 Android assets/ 目录里寻找这个文件名
            //   Metro bundle 命令的 --bundle-output 输出名必须和这里一致
            override fun getJSMainModuleName(): String = "index"

            // ★ 是否开发模式（true = 可以连 Metro dev server 热重载）
            //   这里固定 false：直接加载 assets 里打好的 bundle，不连 dev server
            override fun getUseDeveloperSupport(): Boolean = false

            // ★ 是否启用新架构（Fabric / TurboModules，0.73 默认关闭）
            override val isNewArchEnabled: Boolean = false
            override val isHermesEnabled: Boolean = true  // 使用 Hermes JS 引擎
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        // ★ SoLoader 初始化：加载 RN 依赖的原生 .so 库
        //   在任何 RN 代码运行前必须先调
        SoLoader.init(this, false)
    }
}
