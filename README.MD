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
以上知识点为 Android 入门刚需核心技能，覆盖绝大多数 APP 首页、数据列表页、功能选择页基础开发逻辑。