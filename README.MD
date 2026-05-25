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