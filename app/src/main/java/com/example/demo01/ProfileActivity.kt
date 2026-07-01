package com.example.demo01

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/**
 * Profile Page：支持 General / Location 两个 Tab 切换
 *
 * 数据源：
 * - General Tab: figma-40-381
 * - Location Tab: figma-46-138
 *
 * Tab 切换逻辑：
 * - 选中的 Tab：深紫色背景 (#241C53) + 白字
 * - 未选中的 Tab：灰色背景 + 灰字 (#ADADAD)
 * - 对应的 form LinearLayout 用 VISIBLE / GONE 切换
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel

    // Tab 状态：true=General 选中，false=Location 选中
    private var isGeneralSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupActions()
    }

    private fun setupActions() {
        // 返回按钮
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 顶部编辑按钮
        findViewById<View>(R.id.btnTopEdit).setOnClickListener {
            Toast.makeText(this, "Edit profile", Toast.LENGTH_SHORT).show()
        }

        // 换头像
        findViewById<View>(R.id.avatarEditGroup).setOnClickListener {
            Toast.makeText(this, "Change photo", Toast.LENGTH_SHORT).show()
        }

        // General Tab
        findViewById<View>(R.id.tabGeneral).setOnClickListener {
            switchTab(showGeneral = true)
        }

        // Location Tab
        findViewById<View>(R.id.tabLocation).setOnClickListener {
            switchTab(showGeneral = false)
        }

        // Save Changes 按钮
        findViewById<View>(R.id.btnSaveChanges).setOnClickListener {
            Toast.makeText(this, "Save changes", Toast.LENGTH_SHORT).show()
        }

        // 默认显示 Location Tab（和 XML 初始 visibility 一致）
        switchTab(showGeneral = false)
    }

    /**
     * 切换 Tab
     * @param showGeneral true=显示 General 表单，false=显示 Location 表单
     */
    private fun switchTab(showGeneral: Boolean) {
        isGeneralSelected = showGeneral

        val tabGeneral = findViewById<TextView>(R.id.tabGeneral)
        val tabLocation = findViewById<TextView>(R.id.tabLocation)
        val formGeneral = findViewById<View>(R.id.formGeneral)
        val formLocation = findViewById<View>(R.id.locationForm)

        if (showGeneral) {
            // General 选中：深紫背景 + 白字
            tabGeneral.setBackgroundResource(R.drawable.bg_figma_tab_location_selected)
            tabGeneral.setTextColor(0xFFFFFFFF.toInt())
            // Location 未选中：灰色背景 + 灰字
            tabLocation.setBackgroundResource(R.drawable.bg_figma_tab_general_unselected)
            tabLocation.setTextColor(0xFFADADAD.toInt())
            // 显示 General 表单，隐藏 Location 表单
            formGeneral.visibility = View.VISIBLE
            formLocation.visibility = View.GONE
        } else {
            // Location 选中：深紫背景 + 白字
            tabLocation.setBackgroundResource(R.drawable.bg_figma_tab_location_selected)
            tabLocation.setTextColor(0xFFFFFFFF.toInt())
            // General 未选中：灰色背景 + 灰字
            tabGeneral.setBackgroundResource(R.drawable.bg_figma_tab_general_unselected)
            tabGeneral.setTextColor(0xFFADADAD.toInt())
            // 显示 Location 表单，隐藏 General 表单
            formLocation.visibility = View.VISIBLE
            formGeneral.visibility = View.GONE
        }
    }
}
