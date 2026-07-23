package com.example.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * 占位 Fragment：用于 Rooms / Inbox / Profile 三个 Tab
 * 通过 newInstance 传入标题，复用同一个布局
 *
 * ★ newInstance 工厂方法是 Fragment 传参的标准做法：
 *   - 系统重建 Fragment 时只调用无参构造，不会调用带参构造
 *   - 通过 arguments Bundle 传的数据会被系统自动保存和恢复
 *   - 直接用构造函数传参在旋转屏幕后会丢失
 */
class WanasPlaceholderFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(title: String): WanasPlaceholderFragment {
            val fragment = WanasPlaceholderFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_wanas_placeholder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString(ARG_TITLE) ?: "Tab"
        view.findViewById<TextView>(R.id.tv_placeholder).text = title
    }
}
