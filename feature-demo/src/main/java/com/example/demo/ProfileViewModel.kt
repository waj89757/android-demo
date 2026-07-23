package com.example.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 个人资料页的数据层
 *
 * 职责：
 * 1. 持有页面需要展示的所有数据
 * 2. 对外暴露只读的 LiveData（Activity 只能观察，不能直接修改）
 * 3. 提供模拟加载数据的方法（真实项目这里会调用 Repository → API）
 *
 * Activity 和 ViewModel 的关系：
 *   Activity  →  观察 LiveData，数据变化时自动刷新 UI
 *   ViewModel →  持有数据，Activity 旋转屏幕也不会销毁
 */
class ProfileViewModel : ViewModel() {

    // ── 数据结构：把一个用户的所有信息打包成一个对象 ──────────

    data class UserProfile(
        val name: String,
        val title: String,           // 职位/部门
        val bio: String,             // 个人简介
        val phone: String,
        val email: String,
        val location: String,
        val followingCount: String,  // 关注数
        val followersCount: String,  // 粉丝数
        val likesCount: String       // 获赞数
    )

    // ── LiveData：MutableLiveData 内部可写，对外暴露只读 ──────

    // _profile 是私有的，只有 ViewModel 内部能修改
    private val _profile = MutableLiveData<UserProfile>()

    // profile 是公开的，Activity 通过这个观察，但不能直接赋值
    val profile: LiveData<UserProfile> = _profile

    // 加载状态（用于显示 loading 动画，目前先预留）
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ── 初始化：ViewModel 创建时自动加载数据 ─────────────────

    init {
        loadProfile()
    }

    /**
     * 加载用户资料
     * 现在是硬编码的假数据，以后替换成：
     *   viewModelScope.launch { _profile.value = repository.getProfile() }
     */
    fun loadProfile() {
        _isLoading.value = true

        // TODO: 真实项目替换为网络请求
        _profile.value = UserProfile(
            name           = "王安杰",
            title          = "技术负责人 · 海外增长团队",
            bio            = "学习 Android · 热爱技术 · 持续成长",
            phone          = "138****8888",
            email          = "wanganjie@example.com",
            location       = "北京市",
            followingCount = "128",
            followersCount = "3.2K",
            likesCount     = "1.8W"
        )

        _isLoading.value = false
    }
}
