package com.example.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * ★★★ BouncingBallSurfaceView ★★★
 *
 * 这个自定义 SurfaceView 演示了 SurfaceFlinger 的核心概念：
 *
 *   1. SurfaceView 有自己独立的 Surface（独立图层）
 *      → 普通 View 都共用 App 的一个 Surface
 *      → SurfaceView 是额外的独立图层
 *      → SurfaceFlinger 把它单独合成到最终画面
 *
 *   2. 子线程直接在 Surface 上绘制（普通 View 不允许）
 *      → 通过 SurfaceHolder.lockCanvas() 拿到画布
 *      → 画完后 SurfaceHolder.unlockCanvasAndPost() 提交
 *      → 提交的内容进入 BufferQueue，等 SurfaceFlinger 合成
 *
 *   3. 渲染帧率和主线程 UI 完全独立
 *      → 主线程卡了不影响小球动画
 *      → 小球渲染线程卡了不影响主线程 UI
 */
class BouncingBallSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    // SurfaceHolder：访问 Surface 的句柄
    // lockCanvas() → 拿到画布，可以开始画
    // unlockCanvasAndPost() → 提交这一帧，进入 BufferQueue 等待 SurfaceFlinger 合成
    private val surfaceHolder: SurfaceHolder = holder

    // ★ 渲染线程（子线程）
    private var renderThread: Thread? = null
    private var isRunning = false

    // 小球数据
    data class Ball(
        var x: Float,
        var y: Float,
        var vx: Float,    // x 方向速度
        var vy: Float,    // y 方向速度
        val radius: Float,
        val color: Int
    )

    private val balls = mutableListOf<Ball>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // FPS 统计
    var onFpsUpdate: ((fps: Int, threadName: String) -> Unit)? = null
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()

    // ★ 目标帧率（可动态调整，SurfaceView 不受 Vsync 约束，你说几fps就几fps）
    // 对应每帧 sleep 时间：30fps=33ms，60fps=16ms，120fps=8ms，无限制=0ms
    var targetFps: Int = 60
        set(value) {
            field = value
            frameIntervalMs = if (value <= 0) 0L else (1000L / value)
        }
    private var frameIntervalMs: Long = 16L  // 默认60fps

    private val ballColors = listOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"),
        Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEAA7"),
        Color.parseColor("#DDA0DD"),
        Color.parseColor("#98FB98")
    )

    init {
        // 注册 Surface 生命周期回调
        surfaceHolder.addCallback(this)
        // 背景透明，让下层图层（App 的 Surface）透过来
        setZOrderOnTop(false)
        addBall()  // 初始一个球
    }

    fun addBall() {
        val w = width.takeIf { it > 0 } ?: 400
        val h = height.takeIf { it > 0 } ?: 700
        val r = (40..80).random().toFloat()
        val color = ballColors[balls.size % ballColors.size]
        balls.add(Ball(
            x = (r..w - r).random(),
            y = (r..h / 2f).random(),
            vx = (3..8).random().toFloat() * if ((0..1).random() == 0) 1 else -1,
            vy = (3..8).random().toFloat() * if ((0..1).random() == 0) 1 else -1,
            radius = r,
            color = color
        ))
    }

    fun clearBalls() {
        balls.clear()
    }

    // ===== SurfaceHolder.Callback =====

    /**
     * ★ Surface 创建完成（SurfaceFlinger 已为这个 View 分配了独立图层）
     * 此时可以开始渲染线程
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        // ★ 在子线程里渲染（普通 View 不允许子线程渲染）
        renderThread = Thread {
            renderLoop(holder)
        }.also { it.start() }
    }

    /**
     * ★ Surface 销毁（Activity 退出 / 屏幕熄灭）
     * 必须停止渲染线程，否则线程访问已销毁的 Surface 会崩溃
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        renderThread?.join()  // 等待渲染线程真正结束
        renderThread = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    // ===== 子线程渲染循环 =====

    /**
     * ★★★ 核心：子线程渲染循环 ★★★
     *
     * 这里在子线程里运行，和主线程完全独立。
     * 普通 View 的绘制在主线程，这里不受限制。
     *
     * 每一帧的流程：
     *   1. lockCanvas()        → 从 BufferQueue 取一块空闲 Buffer（作为画布）
     *   2. 在画布上绘制内容
     *   3. unlockCanvasAndPost() → 把画好的 Buffer 放回 BufferQueue
     *   4. SurfaceFlinger 收到 Vsync 信号时从 BufferQueue 取 Buffer，合成到屏幕
     */
    private fun renderLoop(holder: SurfaceHolder) {
        val threadName = Thread.currentThread().name

        while (isRunning) {
            val canvas: Canvas? = holder.lockCanvas()  // ← 取画布（可能阻塞等待空闲 Buffer）
            if (canvas == null) continue

            try {
                // 更新物理状态
                updatePhysics(canvas.width.toFloat(), canvas.height.toFloat())

                // 绘制这一帧
                drawFrame(canvas)

                // 统计 FPS
                countFps(threadName)

            } finally {
                // ★ 必须在 finally 里提交，否则 Buffer 不归还会死锁
                holder.unlockCanvasAndPost(canvas)  // ← 提交这帧到 BufferQueue
            }

            // ★ 用 targetFps 控制帧率
            // frameIntervalMs=0 → 不 sleep，跑满 CPU（展示最高帧率）
            // frameIntervalMs=33 → 30fps
            // frameIntervalMs=16 → 60fps
            // frameIntervalMs=8  → 120fps
            if (frameIntervalMs > 0) Thread.sleep(frameIntervalMs)
        }
    }

    private fun updatePhysics(w: Float, h: Float) {
        balls.forEach { ball ->
            ball.x += ball.vx
            ball.y += ball.vy

            // 碰墙反弹
            if (ball.x - ball.radius < 0) { ball.x = ball.radius; ball.vx = -ball.vx }
            if (ball.x + ball.radius > w) { ball.x = w - ball.radius; ball.vx = -ball.vx }
            if (ball.y - ball.radius < 0) { ball.y = ball.radius; ball.vy = -ball.vy }
            if (ball.y + ball.radius > h) { ball.y = h - ball.radius; ball.vy = -ball.vy }

            // 重力效果
            ball.vy += 0.3f
            if (ball.vy > 20f) ball.vy = 20f
        }
    }

    private fun drawFrame(canvas: Canvas) {
        // 清空背景
        canvas.drawColor(Color.parseColor("#1A1A2E"))

        // 绘制每个球（带径向渐变光泽效果）
        balls.forEach { ball ->
            val shader = RadialGradient(
                ball.x - ball.radius * 0.3f,
                ball.y - ball.radius * 0.3f,
                ball.radius,
                Color.WHITE,
                ball.color,
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawCircle(ball.x, ball.y, ball.radius, paint)

            // 高光
            paint.shader = null
            paint.color = Color.argb(80, 255, 255, 255)
            canvas.drawCircle(
                ball.x - ball.radius * 0.3f,
                ball.y - ball.radius * 0.3f,
                ball.radius * 0.3f, paint
            )
        }

        // 左上角显示渲染线程信息
        paint.shader = null
        paint.color = Color.parseColor("#00FF88")
        paint.textSize = 28f
        canvas.drawText("Render Thread: ${Thread.currentThread().name}", 20f, 40f, paint)
        canvas.drawText("Balls: ${balls.size}", 20f, 75f, paint)
    }

    private fun countFps(threadName: String) {
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTime >= 1000) {
            val fps = frameCount
            frameCount = 0
            lastFpsTime = now
            // 把 FPS 数据回调给主线程（用 post 切回主线程更新 UI）
            post { onFpsUpdate?.invoke(fps, threadName) }
        }
    }

    // 辅助扩展：随机 Float 范围
    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + (Math.random() * (endInclusive - start)).toFloat()
    }

    private fun IntRange.random(): Int = (Math.random() * (last - first + 1)).toInt() + first
}
