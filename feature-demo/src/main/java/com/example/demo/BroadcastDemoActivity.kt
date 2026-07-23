package com.example.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ★★★ BroadcastReceiver Demo ★★★
 *
 * BroadcastReceiver 是 Android 四大组件之一，是事件总线的订阅者。
 * 类比：Redis Pub/Sub，发送者发消息，所有订阅者都能收到，互不知道对方。
 *
 * 工作原理：
 *   发送广播（sendBroadcast / LocalBroadcastManager.sendBroadcast）
 *     ↓ AMS 接收（系统广播）/ LocalBroadcastManager（App 内广播）
 *     ↓ 查找所有匹配 IntentFilter 的 Receiver
 *     ↓ 调用每个 Receiver 的 onReceive()（主线程执行，10秒内必须完成）
 *     ↓ onReceive() 返回后，Receiver 可能被销毁
 *
 * 两种广播：
 *   系统广播：由 Android 系统发送（网络变化、开机、充电等），AMS 负责调度
 *   App 内广播：LocalBroadcastManager，只在 App 内部传递，更安全更高效
 *
 * 两种注册方式：
 *   静态注册（Manifest）：App 未运行时也能收到（如开机广播）
 *   动态注册（代码）：只在 Activity/Service 存活时有效，必须手动注销
 */
class BroadcastDemoActivity : AppCompatActivity() {

    companion object {
        // 自定义广播的 Action（类似 Kafka 的 Topic）
        const val ACTION_CUSTOM = "com.example.demo01.CUSTOM_ACTION"
        const val EXTRA_MESSAGE = "extra_message"
    }

    private lateinit var tvNetworkStatus: TextView
    private lateinit var tvLocalReceived: TextView
    private lateinit var tvLog: TextView

    // ★ 动态注册的网络状态广播接收者
    // 内部类形式，可以直接访问 Activity 的 UI 控件
    private val networkReceiver = object : BroadcastReceiver() {
        /**
         * ★ onReceive：广播到达时由 AMS 在主线程调用
         *
         * 注意：
         *   1. 运行在主线程，不能做耗时操作（超过10秒ANR）
         *   2. 不要在 onReceive 里启动异步任务然后持有 context 引用（context 可能失效）
         *   3. 需要耗时操作 → 启动 Service 或 WorkManager
         */
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            log("系统广播到达: $action")

            when (action) {
                ConnectivityManager.CONNECTIVITY_ACTION,
                "android.net.conn.CONNECTIVITY_CHANGE" -> {
                    updateNetworkStatus(context)
                }
            }
        }
    }

    // ★ App 内广播接收者（LocalBroadcastManager，不经过 AMS）
    // 更安全：只在 App 内部传递，其他 App 收不到
    // 更高效：不需要跨进程 Binder 调用
    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val msg = intent.getStringExtra(EXTRA_MESSAGE) ?: "（无数据）"
            log("App内广播到达，数据: $msg")
            tvLocalReceived.text = "收到广播消息: $msg"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_demo)

        tvNetworkStatus = findViewById(R.id.tv_network_status)
        tvLocalReceived = findViewById(R.id.tv_local_received)
        tvLog           = findViewById(R.id.tv_log)

        // ★★ 动态注册系统广播（网络状态变化）
        // IntentFilter：声明我想接收哪些广播（类比 Kafka Consumer 订阅 Topic）
        registerNetworkReceiver()

        // ★★ 动态注册 App 内广播
        val localFilter = IntentFilter(ACTION_CUSTOM)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(localReceiver, localFilter)

        log("Activity 启动，两个 Receiver 已注册")

        // 初始检测一次网络状态
        updateNetworkStatus(this)

        setupButtons()
    }

    @Suppress("DEPRECATION")
    private fun registerNetworkReceiver() {
        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(networkReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(networkReceiver, filter)
        }
        log("网络状态广播已注册（系统广播，经过 AMS 调度）")
    }

    private fun setupButtons() {
        // ★ 发送自定义 App 内广播（不带数据）
        // LocalBroadcastManager：不经过 AMS，直接在 App 内部路由
        // 安全性：其他 App 无法截获或发送这个广播
        findViewById<Button>(R.id.btn_send_local).setOnClickListener {
            val intent = Intent(ACTION_CUSTOM).apply {
                putExtra(EXTRA_MESSAGE, "普通广播，时间：${currentTime()}")
            }
            log("发送 App 内广播 → LocalBroadcastManager")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            // 注意：这里发完，localReceiver.onReceive() 会同步被调用
        }

        // ★ 发送带数据的广播
        findViewById<Button>(R.id.btn_send_with_data).setOnClickListener {
            val intent = Intent(ACTION_CUSTOM).apply {
                putExtra(EXTRA_MESSAGE, "用户ID=10086，事件=登录成功，时间：${currentTime()}")
            }
            log("发送带数据的 App 内广播")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    @Suppress("DEPRECATION")
    private fun updateNetworkStatus(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val isConnected: Boolean
        val networkType: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(network)
            isConnected = caps != null
            networkType = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "移动数据"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "以太网"
                else -> "无网络"
            }
        } else {
            val info = cm.activeNetworkInfo
            isConnected = info?.isConnected == true
            networkType = info?.typeName ?: "无网络"
        }

        if (isConnected) {
            tvNetworkStatus.text = "✅ 网络已连接：$networkType"
            tvNetworkStatus.setBackgroundColor(0xFFE8F5E9.toInt())
        } else {
            tvNetworkStatus.text = "❌ 无网络连接"
            tvNetworkStatus.setBackgroundColor(0xFFFFEBEE.toInt())
        }

        log("网络状态更新: ${tvNetworkStatus.text}")
    }

    override fun onDestroy() {
        // ★★★ 必须在 onDestroy 注销广播，否则内存泄漏！
        //
        // 原因：BroadcastReceiver 持有 Activity 的引用（访问 UI 控件）
        // 如果不注销，Activity 销毁后 Receiver 仍注册在 AMS / LocalBroadcastManager 里
        // AMS 发广播来时，Receiver 试图操作已销毁的 UI → 崩溃
        // 同时 Activity 对象无法被 GC 回收 → 内存泄漏
        unregisterReceiver(networkReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver)
        log("onDestroy：两个 Receiver 已注销")
        super.onDestroy()
    }

    private fun log(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        tvLog.append("[$time] $msg\n")
    }

    private fun currentTime() =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
