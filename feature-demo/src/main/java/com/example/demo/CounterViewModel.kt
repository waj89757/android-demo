package com.example.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ★ CounterViewModel —— 最简单的 ViewModel + LiveData 示例
 *
 * 只做一件事：存一个计数器
 *
 * ★ MutableLiveData vs LiveData 的关系：
 *
 *   MutableLiveData 就像一个"可写的变量"
 *   LiveData 就像同一个变量的"只读视图"
 *
 *   ┌──────────────────────────────────────┐
 *   │  private val _count = MutableLiveData(0)   │  ← ViewModel 内部可写
 *   │  val count: LiveData<Int> = _count         │  ← 外部只读
 *   │                                              │
 *   │  ViewModel 内部：_count.value = 5           │  ← ✅ 可以改
 *   │  Activity 外部：viewModel.count.value = 5   │  ← ❌ 编译报错！
 *   │                                              │
 *   │  为什么这样设计？                              │
 *   │  类比后端：private setter + public getter    │
 *   │  只有 ViewModel 能改数据                      │
 *   │  Activity 只能读数据、观察变化                  │
 *   └──────────────────────────────────────┘
 */
class CounterViewModel : ViewModel() {

    // ★ 用来追踪 ViewModel 是否是新创建的
    // 第一次创建 = true；旋转屏幕后拿到同一个ViewModel = false
    private var newlyCreated = true

    fun isNewlyCreated(): Boolean {
        if (newlyCreated) {
            newlyCreated = false
            return true
        }
        return false
    }

    // ★ _count 是 MutableLiveData —— ViewModel 内部可以写入
    // 初始值是 0
    private val _count = MutableLiveData(0)

    // ★ count 是 LiveData —— 外部只能读取、不能写入
    // _count 和 count 指向同一个对象，只是 count 暴露的是只读接口
    val count: LiveData<Int> = _count

    // ★ 加1的方法
    // _count.value = 新值 → LiveData 自动通知所有观察者
    // 观察者（Activity）的 observe 回调自动触发
    fun increment() {
        // 读取当前值，加1，写入新值
        val current = _count.value ?: 0
        _count.value = current + 1
        // ★ 这一行就是"触发"！
        // 给 _count 赋新值 → LiveData 检查有没有人在观察这个值
        //   → 有 → 调用那个人的回调函数 → Activity 的 { num -> ... } 自动执行
        //   → 没有或Activity在后台 → 暂存新值，等合适时机再调用
    }

    // ★ 重置方法
    fun reset() {
        _count.value = 0
    }
}