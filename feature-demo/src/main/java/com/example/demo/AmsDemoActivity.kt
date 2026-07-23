package com.example.demo

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ AMS（ActivityManagerService）信息展示 ★★★
 *
 * ActivityManager 是 AMS 对 App 暴露的公开 API 入口。
 * 你通过 getSystemService(ACTIVITY_SERVICE) 拿到它，
 * 底层每次调用都是一次 Binder 调用到 system_server 里的 AMS。
 *
 * ★ AMS 的三大核心职责（通过这个页面直接观察）：
 *
 *   1. 内存管理：给每个进程分配内存上限，监控整体内存压力
 *   2. 进程管理：维护所有运行中的 App 进程列表（OOM adj 打分）
 *   3. Task 管理：维护每个 App 的 Activity 返回栈
 */
class AmsDemoActivity : AppCompatActivity() {

    // ★ ActivityManager 是 AMS 的客户端代理
    // 它持有一个 Binder 引用，你调它的方法 = Binder 调用到 AMS
    private lateinit var activityManager: ActivityManager

    private lateinit var tvMemory: TextView
    private lateinit var tvDeviceMemory: TextView
    private lateinit var tvProcesses: TextView
    private lateinit var tvTasks: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ams_demo)

        // ★ 拿到 ActivityManager（AMS 的 Binder 代理）
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        tvMemory       = findViewById(R.id.tv_memory)
        tvDeviceMemory = findViewById(R.id.tv_device_memory)
        tvProcesses    = findViewById(R.id.tv_processes)
        tvTasks        = findViewById(R.id.tv_tasks)

        loadAmsData()

        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            loadAmsData()
        }
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadAmsData() {
        showMemoryInfo()
        showDeviceMemoryInfo()
        showRunningProcesses()
        showTaskStack()
    }

    /**
     * ★ 1. 当前 App 的内存使用情况
     *
     * AMS 给每个 App 设定了内存上限（largeHeap 可以更大）。
     * Runtime.getRuntime() 获取当前 JVM 堆内存信息。
     */
    private fun showMemoryInfo() {
        val runtime = Runtime.getRuntime()
        val maxMem   = runtime.maxMemory() / 1024 / 1024      // AMS 允许的最大堆内存
        val totalMem = runtime.totalMemory() / 1024 / 1024    // 当前已分配的堆内存
        val freeMem  = runtime.freeMemory() / 1024 / 1024     // 已分配中的空闲部分
        val usedMem  = totalMem - freeMem                     // 实际使用量

        tvMemory.text = """
进程 PID：${android.os.Process.myPid()}
进程 UID：${android.os.Process.myUid()}

AMS 给本 App 分配的堆内存上限：${maxMem}MB
当前已从系统申请的堆内存：    ${totalMem}MB
实际使用中的堆内存：          ${usedMem}MB
堆内存空闲量：               ${freeMem}MB

★ 如果 usedMem 接近 maxMem，
  AMS 会触发 GC，严重时 OOM 崩溃
        """.trimIndent()
    }

    /**
     * ★ 2. 设备整体内存情况
     *
     * AMS 持续监控全局内存压力，
     * 当 availMem < threshold 时开始按 OOM adj 分数杀进程。
     */
    private fun showDeviceMemoryInfo() {
        val memInfo = ActivityManager.MemoryInfo()
        // ★ 这是一次 Binder 调用到 AMS，AMS 填充 memInfo
        activityManager.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / 1024 / 1024
        val availMb = memInfo.availMem / 1024 / 1024
        val thresholdMb = memInfo.threshold / 1024 / 1024
        val usedMb = totalMb - availMb

        tvDeviceMemory.text = """
设备总内存：    ${totalMb}MB
当前可用内存：  ${availMb}MB
当前已用内存：  ${usedMb}MB
内存紧张阈值：  ${thresholdMb}MB

内存是否紧张：  ${if (memInfo.lowMemory) "⚠️ 是！AMS 正在杀后台进程" else "✅ 否，内存充足"}

★ 当可用内存 < 阈值时，
  AMS 开始按 OOM adj 分数从高到低杀进程
  空进程(1000) → 后台App(900) → 后台Service(500) → ...
        """.trimIndent()
    }

    /**
     * ★ 3. 当前运行中的 App 进程
     *
     * AMS 维护一张全局进程表，
     * getRunningAppProcesses() 返回当前用户能看到的进程列表。
     *
     * ★ 注意：Android 7.0 以后出于隐私限制，
     *   只能看到自己 App 的进程，看不到其他 App 的。
     */
    @Suppress("DEPRECATION")
    private fun showRunningProcesses() {
        // ★ Binder 调用 → AMS 返回进程列表
        val processes = activityManager.runningAppProcesses

        if (processes.isNullOrEmpty()) {
            tvProcesses.text = "无法获取（Android 7.0+ 隐私限制，只能看到自己的进程）"
            return
        }

        val sb = StringBuilder()
        // OOM adj 重要性常量对照
        val importanceMap = mapOf(
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND         to "前台（用户正在使用）",
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE to "前台Service（如音乐播放）",
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE            to "可见（被遮住但可见）",
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE            to "后台Service",
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED             to "后台缓存（随时可杀）",
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE               to "已消失"
        )

        processes.forEachIndexed { index, proc ->
            val importanceDesc = importanceMap[proc.importance] ?: "importance=${proc.importance}"
            sb.appendLine("进程${index + 1}：${proc.processName}")
            sb.appendLine("  PID：${proc.pid}")
            sb.appendLine("  重要性：$importanceDesc")
            sb.appendLine()
        }

        tvProcesses.text = sb.toString().trimEnd()
    }

    /**
     * ★ 4. 本 App 的 Task 返回栈
     *
     * Task 是 AMS 维护的 Activity 导航历史。
     * getAppTasks() 返回本 App 的 Task 列表。
     *
     * 你点了多少个页面，这里就能看到 Task 的层级。
     */
    private fun showTaskStack() {
        // ★ Binder 调用 → AMS 返回 Task 列表
        val tasks = activityManager.appTasks

        if (tasks.isNullOrEmpty()) {
            tvTasks.text = "暂无 Task 数据"
            return
        }

        val sb = StringBuilder()
        tasks.forEachIndexed { index, task ->
            val info = task.taskInfo
            sb.appendLine("Task ${index + 1}（ID：${info.taskId}）")
            sb.appendLine("  栈顶 Activity：${info.topActivity?.shortClassName}")
            sb.appendLine("  Activity 数量：${info.numActivities}")
            sb.appendLine("  是否在前台：  ${if (info.isRunning) "是" else "否"}")
            sb.appendLine()
        }

        sb.appendLine("★ 说明：")
        sb.appendLine("  每次 startActivity() 都往栈里 push 一个 Activity")
        sb.appendLine("  按 Back 键弹出栈顶，AMS 恢复下面的 Activity")
        sb.appendLine("  AMS 维护这个栈，不是 App 自己维护的")

        tvTasks.text = sb.toString()
    }
}
