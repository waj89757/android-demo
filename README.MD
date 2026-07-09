Android Demo 全套经典知识点汇总
一、项目环境与构建基础
构建脚本格式
新版 Android Studio 默认使用 Kotlin DSL，配置文件为 build.gradle.kts，摒弃传统 Groovy 语法。
统一版本管理
所有第三方依赖版本统一存放至 gradle/libs.versions.toml，业务层直接引用 libs.xxx，禁止硬写版本号，符合企业级项目规范。
广告依赖闪退踩坑
新建项目若选中广告相关模板，会自动引入谷歌广告 SDK，未配置广告 ID 直接启动 APP 闪退；
解决方式：在AndroidManifest.xml添加广告默认配置绕过校验，或新建纯净空活动项目剔除广告依赖。
日志调试技巧
通过底部Logcat查看运行日志，筛选FATAL EXCEPTION红色崩溃日志，精准定位报错类、报错行数，区分系统日志与业务代码错误。
二、Intent 页面跳转与数据通信
核心作用
Intent 是 Android 四大组件通信核心载体，负责页面跳转、组件间数据传递。
单向页面跳转
通过 Intent 绑定目标 Activity，使用putExtra()以键值对形式传递字符串、基本数据类型，调用startActivity()完成跳转。
新版页面结果回传
废弃老旧startActivityForResult，使用registerForActivityResult注册回调监听，实现 A 页面跳转 B 页面，B 页面处理完成后回传数据给 A 页面。
页面生命周期操作
finish()：关闭当前 Activity，销毁当前页面，自动返回上一级页面，不会销毁上层页面。
传值限制
仅支持基本数据类型、字符串、序列化对象，不支持传递超大对象、视图对象。
三、AlertDialog 交互弹窗
弹窗使用场景区分
Toast：轻量级无交互短时文字提示，仅做信息展示
AlertDialog：正式业务弹窗，用于操作确认、风险提示、功能选择，支持确定 / 取消交互逻辑
核心构建方式
依托AlertDialog.Builder链式调用，设置弹窗标题、提示内容、功能按钮，最后调用show()展示弹窗。
上下文依赖
弹窗必须依附 Activity 上下文创建，页面销毁时弹窗自动销毁，无法独立运行。
四、RecyclerView 高性能列表（核心重点）
控件优势
完全替代传统 ListView，依靠视图复用机制复用滑动消失的条目布局，减少控件创建销毁，滑动流畅、内存占用极低，是 Android 主流列表控件。
列表三层必备结构
外层容器：布局文件中RecyclerView，承载整个列表视图
条目布局：自定义item_xxx.xml，定义单行列表展示样式
自定义 Adapter 适配器：绑定数据源与条目视图，是数据与 UI 的桥梁
Adapter 三大重写核心方法
onCreateViewHolder：加载条目布局，创建视图缓存容器
onBindViewHolder：绑定数据源，为条目内控件赋值
getItemCount：返回列表数据源总数量
ViewHolder 优化原理
提前缓存条目内部所有子控件，避免频繁执行findViewById查找控件，大幅提升列表加载与滑动性能。
列表基础配置
搭配LinearLayoutManager实现竖向常规列表，绑定适配器、注入数据源，自定义方法完成列表数据局部 / 全局刷新。
高频开发踩坑点
未引入 RecyclerView 依赖、布局 ID 不统一、适配器未绑定、数据源为空，均会造成列表空白、空指针异常闪退。
五、Kotlin 安卓编码基础规范
页面控件统一使用lateinit var延迟初始化，规避空指针异常。
同包下直接使用自动生成的R类引用布局、资源文件，无需手动导入 R 类。
入门阶段优先使用传统标准适配器写法，规避简化语法带来的类引用、方法找不到等报错。
日常开发使用List集合模拟后端接口返回数据，贴合真实项目业务场景。
六、ViewModel + LiveData 数据层架构

ViewModel 的本质
ViewModel 是一个生命周期比 Activity 更长的数据容器。Activity 因屏幕旋转等原因销毁重建时，ViewModel 不会销毁，持有的数据完整保留。
通过 ViewModelProvider(this).get(XxxViewModel::class.java) 获取实例，系统保证同一个 Activity 拿到的是同一个对象。
ViewModel 只负责存储数据和业务逻辑，不持有任何 View 引用，不做 UI 操作。

LiveData 的本质
LiveData 是一个"带自动通知能力的变量"。它的底层原理等价于 Python 的 @property setter：

    给 LiveData 赋值（_count.value = 5）时，setter 被拦截，
    框架自动遍历观察者列表，依次调用每个 observe() 注册的回调 lambda，
    无需手动刷新 UI。

    类比 Python：
    @value.setter
    def value(self, new_val):
        self._value = new_val
        for callback in self._observers:  # 遍历所有注册的回调
            callback(new_val)             # 挨个调用

MutableLiveData vs LiveData 的分工
MutableLiveData：ViewModel 内部使用，可读可写，用 private 修饰，外部无法直接赋值。
LiveData：对外暴露，只读，Activity 只能 observe() 监听变化，不能主动写入。
这是标准的"内部 private setter + 外部 public getter"设计，防止外部随意修改数据状态。

observe() 的两种触发时机
第一种（主动触发）：ViewModel 调用 _count.value = 新值，赋值操作立即触发，当前帧内同步执行回调。
第二种（被动推送）：Activity 因旋转销毁重建，新 Activity 重新 observe() 时，LiveData 将当前最新值主动推送给新 Activity，UI 自动恢复，无需任何额外代码。

Lifecycle 感知原理（为什么后台不收通知）
LiveData 不会通知处于后台的 Activity，防止内存泄漏和无效 UI 刷新。
实现方式：AppCompatActivity 内部由 Google 框架注入了一个透明的 ReportFragment，专门感知宿主 Activity 的生命周期事件，只在 Activity 处于 STARTED 或 RESUMED 状态时执行通知，其余状态暂存新值，等 Activity 回到前台再补发。

MVVM 数据流向
用户操作 → View（Activity）调用 ViewModel 方法 → ViewModel 修改 LiveData 值
  → LiveData setter 触发 → 回调 Activity 的 observe lambda → UI 更新。
Activity 只做两件事：触发 ViewModel 方法、在 observe 回调里刷新 UI，不做任何业务逻辑判断。

七、整体学习业务主线
页面路由跳转 → 跨页面双向数据传递 → 客户端交互弹窗设计 → 批量数据流列表渲染 → 网络请求（Retrofit + 协程）→ ViewModel + LiveData 数据层架构

八、AMS（ActivityManagerService）

AMS 的本质
AMS 是 system_server 进程里最核心的系统服务，类比 Kubernetes 控制面：决定 Activity 在哪个进程跑、何时销毁、返回栈如何管理。

AMS 管四件事：
- Activity 生命周期：onCreate/onPause/onResume 等回调全部由 AMS 通过 Binder 发指令，App 执行
- Task 返回栈：维护 Activity 导航历史，Back 键弹栈，多任务切换时整个 Task 移到前后台
- 进程管理（OOM adj）：给进程打分，内存不足时按分数从高到低杀进程
- Service / Broadcast 生命周期调度

OOM adj 优先级（分越低越难被杀）：
- 0：前台 Activity（用户正在使用）
- 200：Foreground Service（音乐播放等）
- 500：后台 Service
- 900：后台 Activity（用户切走的 App）
- 1000：空进程

App 影响 AMS 的合法手段：
- android:largeHeap="true"：申请更大堆内存（默认256MB → 512MB）
- startForeground()：提升进程优先级到 OOM adj=200，必须显示持久通知
- onTrimMemory()：响应 AMS 的内存警告，主动释放缓存
- WorkManager：让 AMS 在合适时机（充电/联网）调度后台任务
- START_STICKY：Service 被杀后 AMS 自动重建

九、Service

Service 的本质
Service 是没有界面的后台组件，默认和 Activity 在同一进程的同一主线程，生命周期独立于 Activity。

两种启动方式：
- startService：一次性启动，Service 自己 stopSelf() 或外部 stopService() 才结束，适合下载、上传日志
- bindService：引用计数管理，最后一个调用 unbindService() 时 AMS 自动销毁，适合音乐播放控制

Service 和进程的关系：
- 默认同进程（不加 android:process 属性）
- android:process=":push" 开独立进程，适合崩溃隔离、突破内存上限、保活，代价是内存翻倍且跨进程通信要 AIDL

独立进程适用场景：
- 崩溃隔离：WebView 进程崩了不影响主进程
- 突破内存上限：图片处理等大内存场景
- 推送保活：国内无 FCM，推送进程常驻接收消息
- 权限隔离：不同进程持有不同敏感权限

十、FCM 与保活

保活的目的
让 App 进程在用户不使用时依然存活，持续接收服务端推来的实时消息（即时消息、直播弹幕等）。

FCM（Firebase Cloud Messaging）
Google 全球推送服务，长连接维护在 Google Play Services（系统进程）而非 App 进程。App 进程死了，GPS 依然帮你接收消息，消息到达后再唤醒 App 处理。海外 App 标配，免费零运维。

国内无法用 FCM 的原因：国内手机无 Google Play Services，且 Google 服务器被封锁。
国内方案：接入厂商推送（小米/华为/OPPO）或自建长连接 + Foreground Service 保活。

App 不能永久存活：系统在架构层面拒绝，OOM adj 机制是系统主权，双进程互保等黑科技被 Android 8.0+ 逐步封堵。

十一、Handler / Looper

核心概念
Handler/Looper 是 Android 的线程通信基础设施，本质是事件循环（Event Loop），等价于 Node.js 的 Event Loop。

三个角色：
- Looper：事件循环，while(true) 从 MessageQueue 取任务执行，没有任务时阻塞等待
- MessageQueue：任务队列，存放待执行的 Message/Runnable
- Handler：投递者+执行者，post()/sendMessage() 往队列投任务，Looper 取出后调 Handler 执行

主线程天然有 Looper（系统在 ActivityThread.main() 里创建）；子线程默认没有 Looper，需要用 HandlerThread。

两种线程：
- 普通 Thread：一次性任务，执行完销毁
- HandlerThread：自带 Looper 的子线程，可持续接收任务，调 quitSafely() 才结束

常用 API：
- mainHandler.post{}：子线程把"更新 UI"的任务投到主线程执行
- postDelayed(runnable, 1000)：延迟1秒后执行
- removeCallbacks(runnable)：撤销还未执行的任务

协程/LiveData 底层都依赖 Handler，withContext(Dispatchers.Main) 内部就是 Handler.post()。

十二、UI 只能主线程操作

铁律：子线程修改 View → 崩溃：CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.

原因三层：
1. View 树是无锁数据结构，多线程并发修改产生竞态，加锁代价太高（60fps每秒数千次操作）
2. Vsync 驱动的 measure/layout/draw 必须串行在主线程执行
3. 所有 GUI 框架的共同设计（iOS/Flutter/Web DOM 都一样）

切回主线程的方法（效果等价）：
- mainHandler.post { textView.text = "x" }
- textView.post { textView.text = "x" }
- runOnUiThread { textView.text = "x" }

十三、SurfaceFlinger 与渲染机制

SurfaceFlinger 的本质
SurfaceFlinger 是 Android 的系统级合成器进程，把屏幕上所有 App 的图层（Surface）按 Z 轴顺序合成成最终一帧画面，送给屏幕显示。你看到的屏幕 = 状态栏图层 + App 内容图层 + 导航栏图层 + 壁纸图层的叠加。

核心概念：
- Surface：内存缓冲区（图层数据），SurfaceFlinger 直接操作，App 写入后通过 BufferQueue 传递
- SurfaceView：Android View 控件，内部持有独立 Surface（独立图层），可在子线程绘制
- Vsync：硬件每 16.67ms（60fps）发一次信号，驱动 Choreographer 触发主线程 measure/layout/draw

普通 View vs SurfaceView：

| | 普通 View | SurfaceView |
|---|---|---|
| 刷新触发 | 被动，等 Vsync | 主动，子线程随时画 |
| 绘制线程 | 主线程 | 子线程 |
| 帧率控制 | 系统控制（最多60fps） | 自己控制（sleep 决定） |
| 用途 | 普通 UI 控件 | 视频、游戏、摄像头预览 |

SurfaceView 渲染三步：
1. lockCanvas()：从 BufferQueue 取一块空闲 Buffer 作为画布
2. 在画布上绘制内容
3. unlockCanvasAndPost()：提交这帧到 BufferQueue，等 SurfaceFlinger 合成

帧率说明：
- 60fps = 16.67ms/帧，90fps = 11.11ms/帧，120fps = 8.33ms/帧
- 画太快（无限制）：SurfaceFlinger 丢多余帧，只显示屏幕刷新率对应的帧，白耗电
- 画太慢（30fps）：用户感觉卡顿

十四、整体学习路线（更新）
页面路由跳转 → 数据传递 → 弹窗交互 → 列表渲染 → 网络请求 → ViewModel/LiveData → MVP → DeepLink → Android 系统架构（Zygote → Binder → AMS → Service → Handler/Looper → SurfaceFlinger）

十五、Canvas

Canvas 是什么
绘图指令的 API 层，提供 drawCircle/drawText/drawBitmap 等方法，底层由 Skia（C++ 图形库）执行，把指令翻译成像素写入绑定的 Bitmap 或 Surface Buffer。

Canvas 不存像素，只是指挥官；Skia 才真正写像素；SurfaceFlinger 把 Buffer 合成到屏幕。

Canvas 必须依附画纸：
- 依附 Bitmap：Canvas(bitmap)，结果写入内存图片
- 依附 Surface：surfaceHolder.lockCanvas()，结果进入 BufferQueue
- 依附 View：系统在 onDraw(canvas) 里自动绑定，最常用

Paint 是搭档：Canvas 决定"画什么形状"，Paint 决定"怎么画"（颜色/描边/渐变/抗锯齿）。

坐标系：原点在左上角，x 向右，y 向下。

自定义 View：重写 onDraw(canvas)，调 invalidate() 触发下一帧重绘。

十六、BroadcastReceiver

本质：事件总线的订阅者，类比 Redis Pub/Sub。发送者发广播，所有注册了匹配 IntentFilter 的 Receiver 都能收到，双方互不知道对方是谁。

工作原理：
发送方 sendBroadcast(intent) → AMS 查所有匹配的 Receiver → 逐个调用 onReceive()（主线程，10秒内完成，超时 ANR）

两类广播：
- 系统广播：AMS 发出（网络变化/开机/充电等），跨进程 Binder 调用
- App 内广播：LocalBroadcastManager，同进程，不过 Binder，更安全更快

两种注册：
- 静态注册（Manifest）：App 未启动也能收（开机广播）
- 动态注册（代码）：Activity/Service 存活时才收，onDestroy 必须 unregisterReceiver，否则内存泄漏

大厂场景：
- 退出登录通知所有页面清空数据
- Push 消息到达通知当前页面刷新
- 下载进度广播

注意：Google 已将 LocalBroadcastManager 标记为 deprecated，现代替代方案是 LiveData / SharedFlow。

十七、ContentProvider

本质：跨进程数据共享的标准接口，类比 REST API。把私有数据库包装成 URI + CRUD 的形式对外暴露，底层走 Binder + 共享内存，用权限系统控制谁能访问。

URI 结构（类似 URL）：
content://com.example.myapp/notes/1
  └── scheme  └── authority（类似域名）  └── path + id

五个标准操作（对应 REST）：
- query()   → GET
- insert()  → POST
- update()  → PUT
- delete()  → DELETE
- getType() → 返回 MIME 类型

URI → Provider 的寻址过程：
1. 解析 URI 中的 authority（com.example.myapp）
2. AMS 在系统注册表（App 安装时写入）里找到对应的 ContentProvider 类名
3. Binder 调用目标进程的 Provider 方法，返回 Cursor

必须在 Manifest 注册，authority 全局唯一，类似域名，不注册则 IllegalArgumentException。

系统内置的 ContentProvider（直接可用）：
- 联系人：ContactsContract.Contacts.CONTENT_URI
- 相册：MediaStore.Images.Media.EXTERNAL_CONTENT_URI
- 短信：content://sms/

大厂场景：
- 多 App 共享登录 token（快手主站 + 极速版同一账号体系）
- 插件化架构：插件进程通过 Provider 访问宿主资源
- FileProvider：App 私有文件安全分享给其他 App（Android 7.0+ 必须用）
- Jetpack App Startup：SDK 利用 Provider.onCreate() 比 Application.onCreate() 更早的时机自动初始化

十八、整体学习路线（更新）
页面路由 → 数据传递 → 弹窗 → 列表 → 网络请求 → MVVM（ViewModel/LiveData）→ MVP → DeepLink
→ 系统架构：Zygote → Binder → AMS → Service → FCM/保活 → Handler/Looper → SurfaceFlinger → Canvas
→ 四大组件：Activity → Service → BroadcastReceiver → ContentProvider
→ Web 技术栈：WebView → JSBridge → 离线包

十九、WebView 与 H5 混合开发

技术选型原则

用 H5（WebView 内嵌网页）：内容驱动、频繁变更、不依赖设备硬件（活动运营页、帮助中心、用户协议）
用 RN（React Native）：有一定功能复杂度、需要跨平台、交互不极致（购物车、设置页、消息列表）
用 Native：性能是核心竞争力或必须访问系统底层（直播间、相机、首页信息流、支付、推送）

WebView 原理

WebView = Chromium 浏览器内核打包成的一个 Android View 控件，内部完整运行 HTML/CSS/JS。
两个世界：Native 世界（Kotlin，有完整系统 API）和 JS 世界（沙盒，默认无设备能力）。
沙盒：给代码一个受限运行环境，跑坏了也不影响外面，JS 不能访问相机/文件系统等。
渲染层面：WebView 有独立 Surface（独立图层），SurfaceFlinger 把它和 Native View 合成到屏幕。
进程风险：WebView 默认和 Native 同进程，H5 OOM 会导致整个 App Crash。
解决：android:process=":webview" 让 WebView 跑独立进程，崩了只死 WebView 进程。

JSBridge（双向通信）

JS 调 Native（addJavascriptInterface 方案）：
  webView.addJavascriptInterface(MyBridge(), "NativeBridge")
  H5 直接调：window.NativeBridge.openCamera()
  必须加 @JavascriptInterface 注解（Android 4.2+ 要求）
  onReceive 在子线程执行，需 runOnUiThread 才能更新 UI

Native 调 JS：
  webView.evaluateJavascript("receiveFromNative('消息')", null)
  H5 提前定义好对应全局函数即可接收

URL 拦截方案（另一种 JS→Native 方式）：
  JS 发假 URL：location.href = "jsbridge://openCamera"
  shouldOverrideUrlLoading 拦截 scheme=jsbridge 的 URL，解析执行

离线包（Offline Package）

本质：把 H5 静态资源（HTML/CSS/JS）提前打包下载到手机本地，用户打开时 WebView 读本地文件而不走网络，白屏时间从 1-2 秒压缩到接近 0。

完整流程：
  1. H5 打包成 zip（含 manifest.json、index.html、css、js）
  2. App 后台下载 zip，校验 md5，解压到私有目录
  3. 加载 manifest.json 建立 URL→本地路径 映射表（HashMap）
  4. shouldInterceptRequest 拦截每个请求：映射表命中→返回本地文件，未命中→放行走网络

manifest.json 格式：
  { "files": { "https://m.example.com/page.html": "index.html", ... } }

和浏览器缓存的区别：缓存是"用了才存"，离线包是"提前下载好"，首次访问也秒开。

UI 适配

最关键：<meta name="viewport" content="width=device-width, initial-scale=1.0">
  没有这行，WebView 用 960px 桌面宽度渲染，字体极小。

安全区适配（刘海/底部手势条）：
  padding-top: env(safe-area-inset-top)
  需配合 viewport-fit=cover 使用

Native 传屏幕信息给 H5：evaluateJavascript 注入 window.nativeScreenInfo
WebView 控制：setSupportZoom(false) 禁止缩放；textZoom=100 固定字体不跟系统走

调试方式：
  webView.setWebContentsDebuggingEnabled(true) + Chrome 打开 chrome://inspect
  直连手机 WebView，实时调 CSS/JS，体验和浏览器开发一样

WebView 常见问题（面试高频）

白屏：预加载 WebView 进程池 + 离线包 + 骨架屏
JSBridge 安全漏洞：恶意 JS 可调注入对象 → @JavascriptInterface + 域名白名单
内存泄漏：WebView 持有 Activity Context，onDestroy 必须 stopLoading() + destroy()
H5/Native 版本不兼容：H5 调用前检测 Bridge 是否存在，提供降级逻辑
Debug：setWebContentsDebuggingEnabled(true)，chrome://inspect 直连

二十一、React 核心知识

为什么要发明 React

纯 JS + HTML 的问题：数据变了，必须手动找到每个 DOM 元素逐个更新，漏一个就是 Bug。
React 的解法：UI = f(state)。状态变了，React 自动重新计算 UI，不需要手动操作 DOM。

名字含义

React = Facebook 2013 年发布的前端框架，核心是声明式 UI + 组件化
Native = 渲染目标是真正的原生控件（不是 HTML），而不是 WebView 里的网页
React Native = 用 React 的编程方式写代码，但渲染成 Android/iOS 原生控件

前端学习路径

HTML/CSS/JS（语言基础）→ React（框架，行业标配）→ React Native（移动端跨平台）

React 五个核心语法特点

1. JSX：在 JS 文件里写类 HTML 结构，{} 里是 JS 表达式
2. 组件：函数返回 UI，大写开头，像 HTML 标签一样使用
3. useState：管理状态，setXxx() 触发 UI 自动重渲染，const [x, setX] = useState(0) 是固定模式
4. useEffect：处理副作用（网络请求/订阅），[] 空数组=只执行一次，类似 onCreate
5. 箭头函数+解构：到处可见，先认识语法再深入

React 四大优点

1. 数据-UI 自动同步：setCart() 一行，所有依赖 cart 的 UI 自动更新，不可能漏
2. 组件复用：DishCard 定义一次，传不同 props 用 N 次，改一处全更新
3. 生态丰富：路由/状态管理/图表/表单/动画，几乎任何需求有现成库
4. 可测试性：业务逻辑是纯函数，和 UI 解耦，单测极简单

JSX 与 React 的关系

JSX 是语法糖，不是 React 本身。浏览器不认识 JSX，必须先编译：
- 开发/演示：Babel 在浏览器里实时编译（type="text/babel"），慢，只用于学习
- 真实项目：Webpack/Vite 提前编译成普通 JS，用户拿到的浏览器直接认识
- 编译结果：<p>{count}</p> → React.createElement("p", null, count)

DOM 与虚拟 DOM

DOM = 浏览器把 HTML 解析后在内存里建的对象树（对应 Android 的 View 树）
操作真实 DOM 很慢（触发重排重绘）
React 先用虚拟 DOM（普通 JS 对象树）在内存里算好差异，再最小化更新真实 DOM
setCount() 触发：重执行函数 → 新虚拟 DOM → Diff → 只改有变化的 DOM 节点

useState 原理

组件是普通函数，每次渲染都重新执行，局部变量会消失。
状态不存在函数里，存在 React 内部的一个数组（stateStorage）里，按 useState 调用顺序对应 slot：
- 第一次执行：slot 没有值 → 用初始值写入
- 后续执行：直接从 slot 取值，初始值被忽略
- setXxx(newVal)：把新值写入 slot → 触发函数重新执行 → UI 更新

const [count, setCount] = useState(0) 是数组解构语法：
  useState 返回 [当前值, setter函数]，解构把两者分别赋给变量，等价于：
  const result = useState(0); const count = result[0]; const setCount = result[1]

规则：useState 必须在函数顶层调用，不能放在 if/for 里。
原因：React 靠调用顺序（slot 下标）识别每个状态，顺序乱了就对应错误。

类比：状态仓库 ≈ ViewModel，组件重渲染 ≈ Activity 重建，从 slot 取值 ≈ 从 ViewModel 取值。

Demo 文件

react/01_vanilla_js.html    纯 JS 餐厅点单（updateAllUI 手动更新 N 处，直观看痛点）
react/02_react.html          React 同一功能（setCart 一行搞定）
react/03_four_features.html  Tab 页演示四大特性（数据同步/组件复用/生态/可测试性）

二十、WebView 进阶学习路线（待学）

第一优先级（直接影响团队日常）：
- JSBridge 协议设计：统一 method/params/callbackId 格式，Bridge 版本管理，避免频繁发版
- 性能监控体系：白屏率、FPS、JS 报错上报（window.onerror），接入监控平台
- 离线包管理流程：发布、版本控制、灰度、回滚机制
- 登录态/Cookie 同步：Native token 通过 CookieManager 注入 WebView，H5 接口自动携带

第二优先级（和 H5 团队协作）：
- H5 路由与 Native 返回键协作（webView.canGoBack() vs finish()）
- 容灾降级：离线包失败→网络兜底，白屏检测→重试页
- 安全：URL 白名单、Bridge 鉴权、HTTPS 强制

第三优先级（深入了解）：
- WebView 预加载优化体系（进程池、DNS 预解析、网络预连接）
- RN vs Flutter vs H5 动态化方案对比
- 小程序架构（特殊的双线程 WebView 方案）- 小程序架构（特殊的双线程 WebView 方案）

二十二、Android 崩溃机制与全局 try-catch 的误区
为什么不能用全局 try-catch 兜住所有异常？
直觉：在 Application 里 Thread.setDefaultUncaughtExceptionHandler，捕获所有未处理异常，让 App 不崩。
现实：能捕获，但不能"继续正常运行"。原因是 Android App 本质是一个状态机。

Android 是一个巨大的状态机
主线程运行一个 Looper，不断从消息队列取消息执行：
  消息1 → 状态A → 状态B
  消息2 → 状态B → 状态C
  消息3 → 状态C → 状态D
每条消息假设"上一条消息已完整执行，状态是正确的"。

当消息2 执行到一半抛出异常，被全局 try-catch 兜住：
  状态停在 A'（既不是 A 也不是 B 的残缺中间态）
  消息3 在 A' 上执行 → 走进了设计者从未预期的路径 → 行为不可控

具体例子：购物车支付流程
正常执行顺序：
  消息1：用户点「结算」→ isPaying=true → 调用创建订单接口
  消息2：网络回调 → orderId="ORD_20240624" → totalPrice=calcTotal() → 跳转支付页
  消息3：支付成功 → isPaying=false → cartItems.clear() → 跳转订单详情

消息2 执行到一半，calcTotal() 内部 NPE，被全局 catch 兜住：
  orderId = "ORD_20240624"  ← 执行了（服务端订单已创建）
  totalPrice = 0             ← 没更新（calcTotal 崩了）
  跳转支付页                 ← 没执行（用户没看到支付页）
  isPaying 还是 true         ← 消息1 留下的残留状态

后续消息在残缺状态上运行的后果：
  消息3：用户再次点「结算」→ 检测到 isPaying==true → "正在支付中" → 按钮点不了
  消息4：服务端订单超时 → 自动取消 orderId → 用户莫名其妙订单被取消
  消息5：用户强退重进 → cartItems 没清 → 再次结算 → 又创建新订单 → 重复扣库存

没有崩溃，但产生了：按钮点不了、重复下单、库存扣错——比直接崩溃更严重。

全局 try-catch 能做什么，不能做什么
  ✅ 能做：捕获崩溃 → 上报日志（Bugly / Sentry） → 干净退出（Process.killProcess）
  ❌ 不能做：捕获后继续正常运行（状态已破坏，继续跑只是延迟更大的崩溃）
  ❌ 看不到：Binder 线程的异常（由 Binder 框架捕获传回调用方，UncaughtExceptionHandler 不可见）
  ❌ 救不了：Native 层资源泄漏（SurfaceView Canvas 没有 unlock、文件句柄没关）

工业级崩溃处理策略
1. UncaughtExceptionHandler → 捕获 + 上报崩溃堆栈 → Process.killProcess() 干净退出
2. Activity 级降级 → 单个页面崩了只关闭该页面，回到上一个正常页面
3. 关键路径局部 try-catch → 支付/登录等核心流程加局部保护，明确知道能恢复的场景
4. 非核心路径让它崩 → 崩了才能暴露问题，日志才能上报，才能修复
5. RN/WebView 沙箱隔离 → JS 线程崩了不影响 Native 主线程（Bridge 隔离的价值之一）

崩溃 vs 未知状态对比
  崩溃：用户看到"程序已停止" → 重启 → 状态从头初始化 → 日志记录准确 → 可复现可修复
  全局兜住：用户继续操作 → 产生错误数据（重复下单/数据不一致） → 无日志 → 无法复现 → 无法修复
  结论：崩溃是"已知的坏"，未知状态继续运行是"未知的坏"，后者对用户和工程师都更危险。

二十三、Android 进程/线程模型与消息驱动全景（信号 → 进程 → 线程 → 代码 → 像素）

本章把散落在各处的系统机制串成一条主线：物理硬件发出信号，如何经过内核、系统进程、App 进程内的各类线程，最终触发你的代码，再把画面推回屏幕。

23.1 进程 vs 线程（最容易混淆，务必分清）
核心区别（房子与工人的比喻）：
- 进程 = 一套独立的房子：有独立内存空间、有门牌号（PID），与其他房子完全隔离，不能直接进对方房间
- 线程 = 房子里干活的工人：可以有多个，共享房子里的一切（内存、文件），各自有独立的工作台（栈）和进度（寄存器）

创建方式完全不同（关键纠错）：
- fork()          → 创建新进程（完整复制当前进程的内存空间）
- pthread_create()→ 在当前进程内创建新线程（和其他线程共享同一块内存）
- 结论：Binder 线程、RenderThread 等都是 pthread 创建的线程，不是 fork 出来的进程；进程内所有线程是兄弟关系，没有父子之分

对比表：
| | 进程（fork创建） | 线程（pthread创建） |
|---|---|---|
| 内存 | 完全独立隔离 | 完全共享 |
| 崩溃影响 | 只影响自己 | 拖垮整个进程 |
| 通信方式 | Binder / Socket / 共享内存 | 直接读写共享内存 |
| 开销 | 大（复制地址空间） | 小（只建栈） |

23.2 fork 与 Zygote（App 冷启动为什么快）
fork 的隔离与共享：
- 隔离：父子进程内存互相看不见，子进程崩溃不影响父进程
- 共享（写时复制 CoW）：fork 瞬间父子共享同一物理内存（只读快照），谁先写某页，内核才复制那一页给它
- 真正共享：已打开的文件描述符、显式 mmap 的共享内存；父死子被 init(PID=1) 收养继续跑，子死父不跟

Zygote 的价值：
- Zygote 预加载了整个 Android Framework 的 class + 资源，JVM 已初始化
- 点击 App 图标 → AMS 通知 Zygote fork 出新 App 进程 → 借 CoW 免费继承预加载内容
- 省去几百 ms 的 JVM 初始化和类加载，这是 App 冷启动快的根因
- 注意：Zygote 崩溃 → Android 的 init 脚本配置为重启整个系统（等于手机重启），并非让 App 裸奔

23.3 Android 软件栈分层与启动顺序
"Android"不是一个进程，而是一层层软件栈：
- Linux 内核：操作系统本身（不是进程），含 Binder 驱动、Input 驱动、DRM 驱动
- init 进程（PID=1）：开机第一个进程，读 init.rc 启动所有服务
- Native 系统进程：SurfaceFlinger、AudioFlinger、mediaserver（C++ 编写，init 直接启动，不经 Zygote）
- Zygote 进程：所有 App 进程的 fork 母体
- system_server 进程：Android 的"大脑"，跑着 80+ 系统服务（AMS/WMS/PMS/IMS 等），由 Zygote fork
- App 进程：普通市民，权限受限，由 Zygote fork

启动顺序：
内核启动 → init(PID=1) → 直接启动 SurfaceFlinger/AudioFlinger 等 Native 进程
         → 启动 Zygote → Zygote fork 出 system_server
用户点图标 → AMS 通知 Zygote fork → App 进程诞生 → ActivityThread.main() → 主线程 Looper 转动

23.4 App 进程内的 5 类线程（你要的全貌）
一个 App 进程（如 com.example.demo01）内部并存多个线程：
| 线程 | 谁创建 | 职责 |
|---|---|---|
| 主线程 / UI线程 | 系统自动（ActivityThread.main） | 生命周期、事件回调、measure/layout/draw，是一切的中枢 |
| Binder 线程池（最多15个） | ART 启动时自动 pthread | 接收其他进程的 Binder 调用，转投主线程 |
| RenderThread | 图形框架自动 | 执行 OpenGL/Vulkan 指令，提交 GPU，不经主线程 |
| OkHttp 线程池 | OkHttp 库 | 真正发 HTTP 网络请求 |
| Dispatchers.IO 协程线程池 | 协程框架 | 磁盘/数据库等耗时 IO |

关键：主线程只做"发号施令"的轻量调度，真正的苦力（GPU渲染/网络/解码）全卸载给其他并行线程和硬件。看似"只有一个主线程"，实则一个精明调度员指挥一群并行工人。主线程唯一铁律：不能做耗时操作，否则整个系统卡顿甚至 ANR。

23.5 两条独立的物理链路（输入 + 输出）
触控输入链（物理 → 你的代码）：
  电容屏（手指）→ Input驱动(/dev/input/eventX) → system_server 的 InputReader/InputDispatcher
  → Socket 发给 App 主线程 → dispatchTouchEvent → onClick 回调
渲染输出链（你的代码 → 物理屏幕）：
  setText/invalidate → 主线程 measure/layout/draw → RenderThread → GPU
  → SurfaceFlinger 合成 → Frame Buffer → DSI总线 → 显示器点亮像素

两条链路硬件不同、内核路径不同、系统进程不同（InputDispatcher vs SurfaceFlinger），
但最终都汇入 App 主线程的同一个 MessageQueue，由 Looper 顺序调度 → 主线程是唯一的"总闸"。

23.6 Binder 线程池 = 进程的"收件室"
- Binder 是 Android 跨进程通信（IPC）机制，底层是内核驱动 /dev/binder，是进程间唯一合法通道
- 每个进程都有自己的 Binder 线程池（不属于 Zygote），进程启动时 ART 自动创建
- 职责单一：接收其他进程发来的 Binder 调用（如 AMS 下发 onCreate/onPause/广播），
  收到后包装成消息投递到主线程 MessageQueue，自己从不执行业务逻辑
- 用线程池（非单线程）是为了多个 Binder 请求可并发接收，互不阻塞
- "系统"下发指令的"系统"= system_server 进程里的 AMS

一次 startActivity 的完整链路（谁触发、谁执行）：
  App A 主线程执行 startActivity()（你的代码主动发起）
  → Binder 跨进程调用 system_server 的 AMS（AMS 是决策者，非发起者）
  → AMS 检查权限/判断目标进程是否存在 → 不存在则通知 Zygote fork 出 App B 进程
  → App B 主线程诞生、Binder线程池建立 → AMS 通过 Binder 告知 App B "执行 onCreate"
  → App B 的 Binder 线程收到，投递到 App B 主线程 MessageQueue
  → App B 主线程 Looper 取出 → 真正执行 onCreate()
结论：startActivity 由你的代码在 App A 主线程触发；onCreate 由 App B 自己的主线程执行；AMS 只是中间调度指挥官。

23.7 MessageQueue 的有序性与淘汰机制
数据结构：按 when（期望执行时间戳）排序的链表，不是数组，也不是严格 FIFO
无容量上限、无自动淘汰，靠以下机制防失控：
- 写入加锁（synchronized）：多线程（Binder/Choreographer/OkHttp/主线程）并发写入不会损坏链表
- 按时间戳排序：postDelayed(100ms) 的消息不会提前执行
- 同类消息去重（removeMessages）：invalidate 100 次，队列里始终只有 1 条 TRAVERSAL，重绘请求自动合并
- ANR 看门狗：单条消息执行超时（Input 5s / 广播 10s / Service 20s）→ system_server 强制 ANR 强杀
- IdleHandler：低优先级任务等队列空闲再执行
- removeCallbacks：主动取消还没执行的消息（如搜索框防抖）

重复点击问题（经典 Bug）：
- 触摸事件 InputDispatcher 不去重，每次点击都独立入队、都触发 onClick、业务逻辑都会重复执行
- 与重绘相反：重绘幂等（画100次=画1次），系统帮你合并；触摸非幂等（点100次含义不同），系统不帮你去重
- 防护责任在开发者：禁用按钮（本项目 btnSaveChanges.isEnabled=false 即此方案）/ 时间戳防抖 / 状态机

23.8 Choreographer 帧调度（VSYNC 与 MessageQueue 的桥梁）
Choreographer = 住在 App 主线程里的"帧调度员/编舞者"，只干一件事：在 VSYNC 到来时触发这一帧渲染。

它如何收到 VSYNC（发布-订阅，方向是主动订阅）：
- SurfaceFlinger 不认识 Choreographer，是 Choreographer 主动通过 Binder 向 SurfaceFlinger 注册"下次 VSYNC 通知我"
- SurfaceFlinger 只维护一张"回调接口列表"，VSYNC 来了逐个回调，完全不知道对面是什么类
- 注册是一次性的：有 invalidate() 才注册才收通知；界面静止 → 不注册 → 不通知 → 主线程零消耗（省电核心）

为什么 SurfaceFlinger 不直接写 App 的 MessageQueue：
- 进程隔离：MessageQueue 是 App 进程内存里的对象，跨进程写内存被 Linux 物理禁止
- 职责分离：SurfaceFlinger 只管"什么时候画"，Choreographer 负责"画什么、怎么画"（哪些 View 脏、有无动画）
- 安全：系统级进程若能直写 App 内存即成后门

它如何驱动渲染（同步屏障 + 异步消息，保证渲染插队）：
- 收到 VSYNC → 往 MessageQueue 插一个"同步屏障"：屏障后的普通（同步）消息全部暂停，Looper 不取
- 再投入一条"异步消息"（渲染任务）：异步消息能穿透屏障被优先取出
- 主线程执行渲染三阶段：INPUT（处理积压触摸）→ ANIMATION（更新动画）→ TRAVERSAL（measure/layout/draw）
- draw 完提交 RenderThread → 移除屏障 → 普通消息恢复
- 效果：渲染帧永远优先于业务消息，保证每帧在预算内完成，不被业务逻辑拖延

23.9 渲染管线最后一公里（合成后如何送到屏幕）
渲染的最终产物是一块存满像素颜色值的内存 = Frame Buffer（帧缓冲区，如 1080×2400×4 ≈ 10MB）。
完整一帧循环：
  VSYNC → Choreographer 触发 → CPU阶段(measure/layout/draw 生成 DisplayList，是绘制指令不是像素)
  → GPU阶段(OpenGL/Vulkan 执行 DisplayList，算出每个像素颜色，写入 Surface Buffer)
  → SurfaceFlinger 合成(叠加 App层+状态栏+导航栏等所有 Surface) → Frame Buffer
  → Display Controller 通过 DSI总线(手机)/HDMI(PC) 逐像素发送 → 屏幕像素点(LCD液晶/OLED)通电发光

双缓冲防撕裂：
- Front Buffer：显示器正在读，显示当前帧；Back Buffer：GPU 正在画下一帧
- VSYNC 时两块内存交换（Swap/Flip），避免画一半就显示导致的画面撕裂（Screen Tearing）

从代码到像素的层次：
  Kotlin(canvas.drawText) → DisplayList(指令,CPU生成) → OpenGL/Vulkan(GPU执行)
  → Surface Buffer(显存/Back) → VSYNC 时 Swap → Frame Buffer(Front) → DSI/HDMI → 屏幕发光

23.10 16.6ms 预算与 CPU/GPU 流水线并行
- 16.6ms（60Hz）是一帧总预算，但 CPU 和 GPU 不是串行，而是错开一帧的流水线并行：
  第N帧 CPU 生成指令的同时，GPU 在执行第N-1帧的指令 → 每个处理器实际只需约一半时间
- 正常 UI 一帧仅需 5-8ms（CPU~4ms + GPU~4ms + 合成~2ms + 余量~6ms），绰绰有余
- 超时（掉帧/Jank）常见原因：主线程做网络/数据库/复杂计算、View 层级太深、过度绘制、大 Bitmap
- 高刷屏预算更严苛：60Hz=16.6ms、90Hz=11.1ms、120Hz=8.3ms、144Hz=6.9ms
- 用 Android Studio CPU Profiler 看 Choreographer.doFrame 每帧耗时，超 16ms 标红即优化入口

23.11 本章全景图（信号 → 进程 → 线程 → 代码 → 像素）

  硬件层         电容屏(手指)                          显示器(发VSYNC)
                    │ I2C/SPI                              │ VSYNC中断
  ────────────────┼──────────────────────────────────────┼────────────────
  Linux内核      Input驱动(/dev/input)                  DRM/KMS驱动
                    │                                      │
  ────────────────┼──────────────────────────────────────┼────────────────
  系统进程       system_server                          SurfaceFlinger进程
                 InputReader/Dispatcher                 监听VSYNC + 合成图层
                    │ Socket                               │ Binder回调(App主动注册)
  ────────────────┼──────────────────────────────────────┼────────────────
  App进程        ┌──────────────────────────────────────────────────────┐
                 │  主线程(UI线程) MessageQueue + Looper  ← 唯一总闸        │
                 │   ├─ 触摸消息 → dispatchTouchEvent → onClick            │
                 │   ├─ Choreographer 插同步屏障+异步消息 → measure/layout/draw │
                 │   └─ Binder线程转投的生命周期消息 → onCreate/onResume    │
                 │  Binder线程池(收件室) / RenderThread(GPU) / OkHttp / 协程IO │
                 └──────────────────────────────────────────────────────┘
                            │ draw生成DisplayList              ↑ 网络结果切回主线程
                            ↓                                  │
                        RenderThread → GPU → SurfaceFlinger合成 → FrameBuffer → DSI → 屏幕发光

一句话总纲：两条物理信号（触摸 + VSYNC）驱动整个系统；所有跨进程沟通走 Binder 这一条内核总线；
Choreographer 用同步屏障让渲染插队；最终所有指令都汇聚到 App 主线程这个单线程队列被顺序执行。
