package com.example.demo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ ServiceDemoActivity —— 演示与 MockPushService 的完整交互 ★★★
 *
 * 这个 Activity 演示两种和 Service 交互的方式：
 *
 *   方式一：startService / stopService
 *     → 只管启动和停止，不能调 Service 内部方法
 *     → Service 和 Activity 生命周期完全独立
 *
 *   方式二：bindService / unbindService
 *     → 拿到 Binder 后可以直接调 Service 的任何 public 方法
 *     → 就像拿到了一个本地对象的引用
 */
class ServiceDemoActivity : AppCompatActivity() {

    // ★ Binder 对象：绑定成功后，通过它拿到 Service 实例
    // null = 未绑定，非 null = 已绑定，可以调 Service 方法
    private var pushService: MockPushService? = null
    private var isBound = false

    private val messages = ArrayDeque<String>(5)

    private lateinit var tvServiceStatus: TextView
    private lateinit var tvBindStatus: TextView
    private lateinit var tvMessageCount: TextView
    private lateinit var tvMessages: TextView

    /**
     * ★ ServiceConnection：bindService 的回调
     *
     * AMS 负责建立连接，连接成功/断开时回调这里
     * 这是 Activity 和 Service 之间的"Binder 桥梁"
     */
    private val serviceConnection = object : ServiceConnection {

        // ★ 绑定成功 → AMS 把 Service 的 Binder 传过来
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            // 把 IBinder 转成我们自己定义的 PushBinder，然后拿到 Service 实例
            val pushBinder = binder as MockPushService.PushBinder
            pushService = pushBinder.getService()  // ← 拿到 Service 对象！
            isBound = true

            updateBindStatus()
            Toast.makeText(this@ServiceDemoActivity,
                "✅ 绑定成功！现在可以直接调 Service 的方法了", Toast.LENGTH_SHORT).show()
        }

        // ★ 连接意外断开（进程崩溃等），不是主动 unbind
        override fun onServiceDisconnected(name: ComponentName?) {
            pushService = null
            isBound = false
            updateBindStatus()
            Toast.makeText(this@ServiceDemoActivity, "Service 连接意外断开", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_demo)

        tvServiceStatus  = findViewById(R.id.tv_service_status)
        tvBindStatus     = findViewById(R.id.tv_bind_status)
        tvMessageCount   = findViewById(R.id.tv_message_count)
        tvMessages       = findViewById(R.id.tv_messages)

        setupButtons()
        observePushService()
    }

    private fun setupButtons() {
        // ★ 启动 Service（startForegroundService）
        // Android 8.0+ 如果要在后台启动 Foreground Service，必须用 startForegroundService
        // 这告诉 AMS："我要启动一个前台服务，5秒内它会调 startForeground()"
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            val intent = Intent(this, MockPushService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)  // ← Android 8.0+ 必须用这个
            } else {
                startService(intent)
            }
            Toast.makeText(this, "已发送启动指令给 AMS，Service 即将启动", Toast.LENGTH_SHORT).show()
        }

        // ★ 停止 Service
        // stopService 后，Service.onDestroy() 被调用，协程取消，通知消失
        findViewById<Button>(R.id.btn_stop_service).setOnClickListener {
            if (isBound) {
                unbindService(serviceConnection)
                isBound = false
                pushService = null
            }
            val intent = Intent(this, MockPushService::class.java)
            stopService(intent)
            Toast.makeText(this, "已通知 AMS 停止 Service", Toast.LENGTH_SHORT).show()
        }

        // ★ 绑定 Service（获取 Binder，可以调 Service 方法）
        // BIND_AUTO_CREATE：如果 Service 还没启动，自动创建它
        findViewById<Button>(R.id.btn_bind_service).setOnClickListener {
            if (isBound) {
                Toast.makeText(this, "已经绑定了", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, MockPushService::class.java)
            // ★ AMS 负责建立连接，连接结果通过 serviceConnection 回调
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Toast.makeText(this, "正在绑定，等待 AMS 回调...", Toast.LENGTH_SHORT).show()
        }

        // ★ 解绑 Service（减少引用计数）
        // 如果没有其他绑定者且没有被 startService，Service 会被销毁
        findViewById<Button>(R.id.btn_unbind_service).setOnClickListener {
            if (!isBound) {
                Toast.makeText(this, "还没绑定", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            unbindService(serviceConnection)
            isBound = false
            pushService = null
            updateBindStatus()
            Toast.makeText(this, "已解绑（引用计数-1）", Toast.LENGTH_SHORT).show()
        }

        // ★ 通过 Binder 直接调用 Service 的方法（绑定后才能用）
        // 这展示了 bindService 的核心价值：像调本地对象一样调 Service
        findViewById<Button>(R.id.btn_send_immediate).setOnClickListener {
            val svc = pushService
            if (svc == null) {
                Toast.makeText(this, "需要先绑定 Service 才能直接调方法", Toast.LENGTH_SHORT).show()
            } else {
                // ★ 这一行：直接调 Service 的方法，就像调本地对象
                svc.sendImmediateMessage("【手动触发】你点击了立即发送按钮")
                Toast.makeText(this, "已调用 service.sendImmediateMessage()", Toast.LENGTH_SHORT).show()
            }
        }

        // ★ 通过 Binder 调用 Service 的清空方法
        findViewById<Button>(R.id.btn_clear_count).setOnClickListener {
            val svc = pushService
            if (svc == null) {
                Toast.makeText(this, "需要先绑定 Service 才能直接调方法", Toast.LENGTH_SHORT).show()
            } else {
                svc.clearMessageCount()  // ★ 直接调 Service 方法
                tvMessageCount.text = "累计收到消息：0 条（已清空）"
                Toast.makeText(this, "已调用 service.clearMessageCount()", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    /**
     * ★ 观察 Service 暴露的 LiveData
     *
     * 注意：Service 和 Activity 可能不在同一个生命周期
     * 用全局 LiveData（companion object 里的）让 Service 可以跨生命周期推消息
     */
    private fun observePushService() {
        // 观察 Service 运行状态
        MockPushService.isRunning.observe(this) { running ->
            if (running) {
                tvServiceStatus.text = "Service 状态：运行中（Foreground Service，OOM adj=200）"
                tvServiceStatus.setBackgroundColor(0xFF1B5E20.toInt())
            } else {
                tvServiceStatus.text = "Service 状态：已停止"
                tvServiceStatus.setBackgroundColor(0xFFD32F2F.toInt())
            }
        }

        // 观察 Service 推来的新消息
        MockPushService.newMessage.observe(this) { msg ->
            // 只保留最新5条
            if (messages.size >= 5) messages.removeFirst()
            messages.addLast(msg)

            tvMessages.text = messages.joinToString("\n\n")
            tvMessageCount.text = "累计收到消息：${pushService?.getMessageCount() ?: messages.size} 条"
        }
    }

    private fun updateBindStatus() {
        if (isBound) {
            tvBindStatus.text = "Binding 状态：✅ 已绑定！可直接调用 Service 方法（通过 Binder）"
            tvBindStatus.setBackgroundColor(0xFFC8E6C9.toInt())
        } else {
            tvBindStatus.text = "Binding 状态：未绑定（无法直接调用 Service 方法）"
            tvBindStatus.setBackgroundColor(0xFFFFF9C4.toInt())
        }
    }

    override fun onDestroy() {
        // ★ Activity 销毁时必须解绑，否则 Context 泄漏
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }
}
