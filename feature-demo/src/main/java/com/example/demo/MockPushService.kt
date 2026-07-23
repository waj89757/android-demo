package com.example.demo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ★★★ MockPushService —— 模拟推送服务 ★★★
 *
 * 这个 Service 演示了三件事：
 *
 *   1. Foreground Service：startForeground() 提升进程优先级
 *      → AMS 不会轻易杀掉本进程（OOM adj 从 900 降到 200）
 *
 *   2. 绑定型 Service（Binder）：Activity 通过 bindService() 拿到 Binder 对象，
 *      可以直接调用 Service 的方法（就像调本地对象一样）
 *
 *   3. 后台持续任务：用协程模拟"每5秒收到一条新推送消息"
 *      → 即使 Activity 退出，Service 继续运行，消息继续产生
 */
class MockPushService : Service() {

    companion object {
        private const val TAG = "MockPushService"
        private const val CHANNEL_ID = "push_channel"
        private const val NOTIFICATION_ID = 1001

        // ★ Activity 通过 LiveData 接收消息
        // 这里用全局 LiveData 简化演示（实际项目用 EventBus 或 Room）
        val newMessage = androidx.lifecycle.MutableLiveData<String>()
        val isRunning = androidx.lifecycle.MutableLiveData(false)
    }

    // ★★ Binder 是连接 Activity 和 Service 的"桥梁"
    //
    // 工作原理：
    //   Activity 调用 bindService() → AMS 通知 Service 创建 Binder →
    //   AMS 把 Binder 传回给 Activity → Activity 拿到 Binder 后
    //   就可以直接调用 Service 的方法了
    //
    // 这里的 Binder 是"本地 Binder"（同进程）
    // 跨进程时要用 AIDL 生成的 Binder，但原理相同
    inner class PushBinder : Binder() {
        // ★ 通过这个方法，Activity 拿到 Service 实例
        fun getService(): MockPushService = this@MockPushService
    }

    private val binder = PushBinder()
    private var messageCount = 0
    private var simulationJob: Job? = null

    // 模拟消息内容池
    private val mockMessages = listOf(
        "你的好友张三给你发了一条消息",
        "【订单通知】你的包裹已发货，预计明天到达",
        "【促销】双11大促开始啦！限时折扣",
        "你的帖子收到了一个新评论",
        "系统维护通知：今晚 23:00-24:00 停服",
        "你有一个新的好友请求",
        "【安全提醒】你的账户在新设备登录",
        "直播提醒：你关注的主播开播了"
    )

    // ===================== Service 生命周期 =====================

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "★ onCreate() — Service 第一次创建（AMS 负责调用这里）")
        Log.d(TAG, "  进程 PID: ${android.os.Process.myPid()}")
        Log.d(TAG, "  运行在主线程: ${android.os.Looper.myLooper() == android.os.Looper.getMainLooper()}")

        createNotificationChannel()
    }

    /**
     * ★ 每次 startService() / startForegroundService() 都会调这里
     *
     * 返回值告诉 AMS：被杀后怎么处理？
     *   START_STICKY        → 重建 Service，不重发 Intent（适合音乐播放、长连接）
     *   START_NOT_STICKY    → 不重建（适合一次性任务）
     *   START_REDELIVER_INTENT → 重建 Service，重发最后一个 Intent（适合下载）
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "★ onStartCommand() — 收到启动指令，startId=$startId")

        // ★★ Foreground Service 的关键调用
        // 必须在 onStartCommand 里（Android 8.0 要求5秒内调用，否则 ANR）
        // 效果：进程 OOM adj 从 900（后台可杀）降到 200（前台服务，极难被杀）
        startForeground(NOTIFICATION_ID, buildNotification("推送服务运行中..."))

        isRunning.postValue(true)
        startMessageSimulation()

        return START_STICKY  // 被杀后 AMS 自动重建
    }

    /**
     * ★ bindService() 时调用
     * 返回 Binder 对象给 Activity，Activity 就可以调用 Service 的方法了
     */
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "★ onBind() — Activity 绑定了 Service（引用计数 +1）")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "★ onUnbind() — Activity 解绑了 Service（引用计数 -1）")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "★ onDestroy() — Service 销毁，清理所有资源")
        simulationJob?.cancel()  // ← 必须取消协程，否则协程泄漏
        isRunning.postValue(false)
        super.onDestroy()
    }

    // ===================== Service 对外暴露的业务方法 =====================
    // Activity 通过 Binder.getService() 拿到 Service 实例后，可以直接调这些方法

    /** Activity 主动获取最新消息条数 */
    fun getMessageCount(): Int = messageCount

    /** Activity 主动清空消息计数 */
    fun clearMessageCount() {
        messageCount = 0
        updateNotification("推送服务运行中（已清空）")
        Log.d(TAG, "消息计数已清空")
    }

    /** Activity 主动触发一条立即推送 */
    fun sendImmediateMessage(content: String) {
        handleNewMessage(content)
    }

    // ===================== 内部实现 =====================

    /**
     * ★ 模拟推送消息接收
     *
     * 真实场景：这里是一个长连接（TCP/WebSocket）在不断接收服务端推来的数据
     * 这里用协程定时模拟
     */
    private fun startMessageSimulation() {
        simulationJob?.cancel()
        simulationJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "模拟推送开始，每5秒收一条消息（跑在 IO 线程，Service 运行在主线程）")
            var index = 0
            while (true) {
                delay(5000)  // 模拟网络延迟/服务端推送间隔
                val msg = mockMessages[index % mockMessages.size]
                index++
                handleNewMessage(msg)
            }
        }
    }

    private fun handleNewMessage(content: String) {
        messageCount++
        Log.d(TAG, "收到新消息[$messageCount]: $content")

        // ★ postValue：从任意线程更新 LiveData（setValue 只能主线程）
        // Activity 通过观察 newMessage 这个 LiveData 实时感知新消息
        newMessage.postValue("[$messageCount] $content")

        // 更新通知栏显示
        updateNotification("新消息($messageCount): $content")
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, ServiceDemoActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Demo01 推送服务")
            .setContentText(content)
            .setContentIntent(pendingIntent)  // 点通知打开 ServiceDemoActivity
            .setOngoing(true)                 // 不可滑动删除（Foreground Service 特征）
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "推送通知",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
