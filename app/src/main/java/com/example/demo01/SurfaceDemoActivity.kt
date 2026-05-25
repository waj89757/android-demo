package com.example.demo01

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ★★★ SurfaceFlinger Demo ★★★
 *
 * SurfaceFlinger 是 Android 的合成器（系统进程）：
 *   - 屏幕上每个 App 的画面 = 一个图层（Surface）
 *   - SurfaceFlinger 把所有图层按 Z 轴顺序合成 → 一帧画面 → 送给屏幕
 *
 * 这个 Demo 用 SurfaceView 演示：
 *   - SurfaceView 有独立 Surface（独立图层）
 *   - 小球在子线程渲染，SurfaceFlinger 独立合成这个图层
 *   - 主线程的 UI（FPS 文字、按钮）和小球渲染互不影响
 */
class SurfaceDemoActivity : AppCompatActivity() {

    private lateinit var surfaceView: BouncingBallSurfaceView
    private lateinit var tvFps: TextView
    private lateinit var tvThreadInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface_demo)

        surfaceView   = findViewById(R.id.surface_view)
        tvFps         = findViewById(R.id.tv_fps)
        tvThreadInfo  = findViewById(R.id.tv_thread_info)

        surfaceView.onFpsUpdate = { fps, threadName ->
            tvFps.text = "实际 FPS: $fps  |  目标: ${surfaceView.targetFps.let { if (it == 0) "无限制" else "${it}fps" }}"
            tvThreadInfo.text = "渲染线程: $threadName（主线程是 main，这是子线程）"
        }

        // 帧率切换按钮
        // 30fps：sleep 33ms，小球移动明显卡顿
        // 60fps：sleep 16ms，正常流畅
        // 120fps：sleep 8ms，肉眼看和60fps差不多（受屏幕硬件限制）
        // 无限制：不 sleep，CPU 跑满，实际帧数取决于设备性能（可能几百fps）
        //         但屏幕还是只显示 60/120fps，多余的帧被 SurfaceFlinger 丢掉
        findViewById<Button>(R.id.btn_fps_30).setOnClickListener {
            surfaceView.targetFps = 30
        }
        findViewById<Button>(R.id.btn_fps_60).setOnClickListener {
            surfaceView.targetFps = 60
        }
        findViewById<Button>(R.id.btn_fps_120).setOnClickListener {
            surfaceView.targetFps = 120
        }
        findViewById<Button>(R.id.btn_fps_max).setOnClickListener {
            surfaceView.targetFps = 0  // 0 = 不 sleep，跑满
        }

        findViewById<Button>(R.id.btn_add_ball).setOnClickListener {
            surfaceView.addBall()
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            surfaceView.clearBalls()
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
