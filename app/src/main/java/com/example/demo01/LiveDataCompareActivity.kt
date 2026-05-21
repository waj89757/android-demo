package com.example.demo01

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
 * ★★★ LiveData 对比 Demo —— 最直观的对比实验 ★★★
 *
 * 这个页面左右对比，让你亲眼看到 LiveData 的作用：
 *
 * ┌──────────────────┬──────────────────┐
 * │ ❌ 没有 LiveData   │ ✅ 有 LiveData     │
 * │                    │                    │
 * │ 普通变量存储        │ ViewModel+LiveData │
 * │                    │                    │
 * │ 计数：0            │ 计数：0            │
 * │                    │                    │
 * │ [+1按钮]          │ [+1按钮]          │
 * │                    │                    │
 * │ ★ 旋转屏幕后：     │ ★ 旋转屏幕后：     │
 * │   计数丢失→回到0   │   计数还在→自动恢复 │
 * └──────────────────┴──────────────────┘
 *
 * ★ 核心原理图：
 *
 * 【没有 LiveData】：
 *   Activity普通变量 = 5
 *     ↓ 旋转屏幕
 *   Activity销毁 → 变量没了
 *     ↓ Activity重新创建
 *   Activity普通变量 = 0  ← 丢了！
 *
 * 【有 LiveData】：
 *   ViewModel里的LiveData.value = 5
 *     ↓ 旋转屏幕
 *   Activity销毁 → 但ViewModel还活着！
 *     ↓ Activity重新创建
 *   新Activity.observe() → LiveData发现值是5 → 自动回调 → UI显示5
 *
 * ★ 旋转屏幕会发生什么？
 *   1. Activity.onDestroy() 被调用 → Activity对象销毁
 *   2. Activity.onCreate() 重新被调用 → 创建全新的Activity对象
 *   3. ViewModel 不销毁（它的生命周期比Activity长）
 *   4. LiveData 检查：有新的Activity在观察我吗？
 *      → 有 → 把当前值 5 发给新Activity的回调 → UI自动更新
 */
class LiveDataCompareActivity : AppCompatActivity() {

    // ★★★ 左边：没有 LiveData —— 普通Activity变量 ★★★
    // 就是一个普通的 Int 变量，存在Activity对象里
    // Activity销毁 = 这个变量没了
    private var plainCount = 0

    // ★★★ 右边：有 LiveData —— ViewModel ★★★
    // ViewModelProvider(this) 保证：
    //   - 第一次调用：创建新的ViewModel
    //   - 旋转屏幕后再调用：返回同一个ViewModel（它没被销毁）
    private val viewModel: CounterViewModel by lazy {
        ViewModelProvider(this).get(CounterViewModel::class.java)
    }

    // ★ 重建次数追踪（用来证明Activity被重建了）
    companion object {
        private var activityRebuildCount = 0
        private var viewModelCreateCount = 0
    }

    private lateinit var tvNoLiveDataCount: TextView
    private lateinit var tvNoLiveDataRebuild: TextView
    private lateinit var tvLiveDataCount: TextView
    private lateinit var tvLiveDataVmCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_live_data_compare)

        // ★ 每次onCreate被调用，activityRebuildCount+1
        // 旋转屏幕 = Activity销毁 + Activity重建 = onCreate重新被调用
        activityRebuildCount++
        // ★ viewModelCreateCount 只有第一次创建ViewModel时才+1
        // 旋转屏幕不会创建新的ViewModel
        if (viewModel.isNewlyCreated()) {
            viewModelCreateCount++
        }

        tvNoLiveDataCount = findViewById<TextView>(R.id.tv_no_live_data_count)
        tvNoLiveDataRebuild = findViewById<TextView>(R.id.tv_no_live_data_rebuild)
        tvLiveDataCount = findViewById<TextView>(R.id.tv_live_data_count)
        tvLiveDataVmCount = findViewById<TextView>(R.id.tv_live_data_vm_count)

        // ★★★ 左边：没有 LiveData ★★★
        // 普通变量，旋转屏幕后丢失
        // 每次 onCreate 都要重新显示 plainCount
        // 但 plainCount 是新Activity的变量，默认值 = 0
        // 所以旋转后左边永远回到 0
        tvNoLiveDataCount.text = plainCount.toString()
        tvNoLiveDataRebuild.text = "Activity重建次数：$activityRebuildCount"

        findViewById<Button>(R.id.btn_no_live_data_plus).setOnClickListener {
            plainCount++
            tvNoLiveDataCount.text = plainCount.toString()
        }
        findViewById<Button>(R.id.btn_no_live_data_reset).setOnClickListener {
            plainCount = 0
            tvNoLiveDataCount.text = plainCount.toString()
        }

        // ★★★ 右边：有 LiveData ★★★
        // ★★ 这就是 LiveData 的核心 —— observe() ★★
        //
        // observe(this, { num -> ... }) 的含义：
        //
        //   this = 当前Activity（LifecycleOwner）
        //   { num -> ... } = 回调函数
        //
        // 当 LiveData 的值发生变化时：
        //   1. LiveData 检查：这个Activity是否在前台？
        //      → 是 → 立刻调用回调函数 { num -> tvLiveDataCount.text = num.toString() }
        //      → 否 → 暂存新值，等Activity回到前台再调用
        //
        // 当 Activity 重建（旋转屏幕）后重新调用 observe()：
        //   LiveData 发现：有个新Activity在观察我
        //   LiveData 检查：我当前的值是什么？
        //   → 把当前值立刻发给新Activity的回调 → UI自动恢复
        //
        // ★ 类比后端：
        //   observe() 就像 Kafka 消费者订阅一个 topic
        //   LiveData 就像 topic 里的最新消息
        //   新消费者加入 → 先收到最新的消息 → 然后持续接收新消息
        viewModel.count.observe(this) { num ->
            // ★ 这个回调是谁触发的？
            // 不是你手动调用的！
            // 是 LiveData 在检测到值变化后，自动调用的
            //
            // 触发时机：
            // 1. viewModel.increment() → _count.value = 新值 → LiveData通知 → 这里被调用
            // 2. 旋转屏幕 → 新Activity.observe() → LiveData把已有值发给新Activity → 这里被调用
            tvLiveDataCount.text = num.toString()
        }

        tvLiveDataVmCount.text = "ViewModel创建次数：$viewModelCreateCount"

        findViewById<Button>(R.id.btn_live_data_plus).setOnClickListener {
            // ★ 点击+1 → viewModel.increment()
            // → _count.value = 当前值+1
            // → LiveData检测到值变化
            // → 自动调用上面 observe 注册的回调
            // → tvLiveDataCount.text 自动更新
            viewModel.increment()
        }
        findViewById<Button>(R.id.btn_live_data_reset).setOnClickListener {
            viewModel.reset()
        }

        findViewById<Button>(R.id.btn_back_main).setOnClickListener {
            finish()
        }
    }

    // ★ onSaveInstanceState —— onSaveInstanceState 也能保存数据！
    // 但 LiveData 方案更优雅，不需要手动保存/恢复
    // 这里故意不用 onSaveInstanceState，让左边彻底丢失
    // 这样你才能看到对比效果
}