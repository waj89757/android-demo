package com.example.demo01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * ★ 个人主页 Fragment —— 演示 Fragment 向 Activity 通信
 *
 * Fragment 与 Activity 的通信方式：
 * ① Fragment → Activity：通过接口回调（最标准的方式）
 * ② Activity → Fragment：通过 Bundle arguments（已演示）
 * ③ Fragment → Fragment：通过共享 ViewModel（后续学习）
 */
class ProfileFragment : Fragment() {

    // ★ 定义接口 —— Fragment 通过这个接口跟宿主 Activity 通信
    // Activity 实现这个接口，Fragment 调用接口方法把消息传给 Activity
    interface OnProfileClickListener {
        fun onMessageFromFragment(message: String)
    }

    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_LEVEL = "level"

        fun newInstance(userName: String, userId: Int, level: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_USER_NAME, userName)
            args.putInt(ARG_USER_ID, userId)
            args.putString(ARG_LEVEL, level)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var tvUserName: TextView
    private lateinit var tvUserId: TextView
    private lateinit var tvUserLevel: TextView
    private lateinit var btnSend: Button

    // ★ 保存 Activity 传过来的接口引用
    private var listener: OnProfileClickListener? = null

    // ★ onAttach —— Fragment 被添加到 Activity 时调用
    // 这里做接口绑定：检查 Activity 是否实现了我们定义的接口
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        // 如果宿主 Activity 实现了接口，就保存引用
        if (context is OnProfileClickListener) {
            listener = context
        } else {
            // 没实现就抛异常，强制宿主 Activity 必须实现这个接口
            throw RuntimeException("宿主 Activity 必须实现 OnProfileClickListener 接口")
        }
    }

    // ★ onDetach —— Fragment 从 Activity 移除时调用
    // 清除接口引用，防止内存泄漏
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserId = view.findViewById(R.id.tv_user_id)
        tvUserLevel = view.findViewById(R.id.tv_user_level)
        btnSend = view.findViewById(R.id.btn_send_to_activity)

        // 从 arguments Bundle 取出数据并展示
        val userName = arguments?.getString(ARG_USER_NAME, "未知用户")
        val userId = arguments?.getInt(ARG_USER_ID, 0)
        val level = arguments?.getString(ARG_LEVEL, "普通用户")

        tvUserName.text = "用户名：$userName"
        tvUserId.text = "用户ID：$userId"
        tvUserLevel.text = "等级：$level"

        // ★ Fragment → Activity 通信：通过接口回调
        btnSend.setOnClickListener {
            val message = "来自 ProfileFragment 的消息：你好 Activity！"
            // 调用接口方法，实际执行的是 Activity 里的实现
            listener?.onMessageFromFragment(message)
        }
    }
}