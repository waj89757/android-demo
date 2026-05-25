package com.example.demo01

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ★★★ Handler / Looper Demo ★★★
 *
 * Handler/Looper 是 Android 的线程通信机制，本质是事件循环（Event Loop）。
 *
 * 三个核心角色：
 *   Looper       → 事件循环，while(true) 不断从 MessageQueue 取任务执行
 *   MessageQueue → 任务队列，存放待执行的 Message/Runnable
 *   Handler      → 投递者 + 执行者，往队列投任务，Looper 取出后交给 Handler 执行
 *
 * 主线程天然有 Looper（系统在 ActivityThread.main() 里创建的）
 * 子线程默认没有 Looper，要用 HandlerThread 或手动 Looper.prepare()
 */
class HandlerDemoActivity : AppCompatActivity() {

    // ★ 主线程的 Handler（绑定主线程的 Looper）
    // 任何线程都可以通过这个 Handler 把任务投到主线程执行
    private val mainHandler = Handler(Looper.getMainLooper())

    // ★ 子线程的 Handler（HandlerThread 自带 Looper）
    // HandlerThread = Thread + Looper，专门用来在子线程处理消息
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private lateinit var tvThreadResult: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var tvHandlerThread: TextView
    private lateinit var tvLog: TextView

    private var countdownSeconds = 0
    private val countdownRunnable = object : Runnable {
        override fun run() {
            if (countdownSeconds > 0) {
                tvCountdown.text = "倒计时：$countdownSeconds 秒"
                countdownSeconds--
                // ★ postDelayed：延迟1秒后再执行自己（递归实现倒计时）
                mainHandler.postDelayed(this, 1000)
            } else {
                tvCountdown.text = "倒计时：结束！"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handler_demo)

        tvThreadResult  = findViewById(R.id.tv_thread_result)
        tvCountdown     = findViewById(R.id.tv_countdown)
        tvHandlerThread = findViewById(R.id.tv_handler_thread)
        tvLog           = findViewById(R.id.tv_log)

        initBackgroundThread()
        setupButtons()
    }

    private fun initBackgroundThread() {
        // ★ HandlerThread：自带 Looper 的子线程
        // 内部实现：Thread { Looper.prepare(); Looper.loop() }
        backgroundThread = HandlerThread("MyBackgroundThread")
        backgroundThread.start()  // 启动线程，Looper 开始循环

        // ★ 创建绑定到这个子线程 Looper 的 Handler
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun setupButtons() {

        // ===== 场景一：子线程 → 主线程 =====
        findViewById<Button>(R.id.btn_thread_to_main).setOnClickListener {
            log("点击按钮，准备启动子线程")

            // ★ 开一个普通子线程（没有 Looper）
            Thread {
                log("子线程开始执行，模拟网络请求...")
                Thread.sleep(2000)  // 模拟耗时操作
                val result = "数据加载完成！时间：${currentTime()}"

                // ★★ 关键：子线程不能直接修改 UI，必须通过 Handler 投到主线程
                // mainHandler.post { ... } 把这个 lambda 包装成 Message
                // 投到主线程的 MessageQueue，主线程的 Looper 取出来执行
                mainHandler.post {
                    log("主线程收到任务，更新 UI")
                    tvThreadResult.text = result  // ← 这行代码在主线程执行 ✅
                }

                log("子线程任务投递完成，子线程结束")
            }.start()
        }

        // ===== 场景二：延迟任务（倒计时） =====
        findViewById<Button>(R.id.btn_start_countdown).setOnClickListener {
            log("开始10秒倒计时")
            countdownSeconds = 10
            tvCountdown.text = "倒计时：10 秒"

            // ★ postDelayed：延迟指定时间后执行
            // 内部实现：Message 带一个 when 字段（执行时间戳）
            // Looper 取任务时检查 when，没到时间就继续等待
            mainHandler.postDelayed(countdownRunnable, 1000)
        }

        findViewById<Button>(R.id.btn_stop_countdown).setOnClickListener {
            log("停止倒计时")
            // ★ removeCallbacks：从 MessageQueue 里移除还没执行的任务
            mainHandler.removeCallbacks(countdownRunnable)
            tvCountdown.text = "倒计时：已停止"
        }

        // ===== 场景三：HandlerThread（子线程的 Looper） =====
        findViewById<Button>(R.id.btn_post_to_bg).setOnClickListener {
            log("向子线程投递任务")

            // ★ backgroundHandler 绑定的是子线程的 Looper
            // 这个 post 会把任务投到子线程的 MessageQueue
            // 子线程的 Looper 取出来执行
            backgroundHandler.post {
                log("子线程收到任务，开始执行耗时操作")
                Thread.sleep(1000)  // 模拟耗时操作
                val result = "子线程处理完成：${currentTime()}"

                // ★ 子线程处理完后，再投回主线程更新 UI
                mainHandler.post {
                    log("主线程更新 UI")
                    tvHandlerThread.text = result
                }
            }
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    /**
     * ★ 日志方法：记录当前线程名
     * 这样能清楚看到每段代码在哪个线程执行
     */
    private fun log(msg: String) {
        val threadName = Thread.currentThread().name
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMsg = "[$time] [$threadName] $msg\n"

        // ★ 日志必须在主线程更新 UI
        // 如果当前就是主线程，直接更新；否则投到主线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            tvLog.append(logMsg)
        } else {
            mainHandler.post {
                tvLog.append(logMsg)
            }
        }
    }

    private fun currentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onDestroy() {
        // ★ 必须清理：移除所有待执行的任务，停止 HandlerThread
        mainHandler.removeCallbacks(countdownRunnable)
        backgroundThread.quitSafely()  // 让 Looper 退出循环，线程结束
        super.onDestroy()
    }
}
