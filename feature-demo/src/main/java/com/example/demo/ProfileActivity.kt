package com.example.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.network.ProfileRequest
import com.example.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Profile Page：支持 General / Location 两个 Tab 切换
 *
 * Save Changes 流程：
 * 1. 禁用按钮 + 改文字为 Saving...
 * 2. 收集表单字段，序列化日志
 * 3. 通过 Retrofit 发起 POST /api/profile/save（MockInterceptor 拦截，不真实出网络）
 * 4. 根据 HTTP 状态码弹出成功 / 失败弹窗
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel

    private var isGeneralSelected = false

    // ===== General Tab 可编辑字段 =====
    private lateinit var etGender: EditText
    private lateinit var etCategory: EditText
    private lateinit var etMobile: EditText
    private lateinit var etParentEmail: EditText
    private lateinit var etFatherName: EditText
    private lateinit var etMotherName: EditText

    // ===== Location Tab 可编辑字段 =====
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etState: EditText

    // ===== Save 按钮 =====
    private lateinit var btnSaveChanges: TextView

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        initView()
        setupActions()
    }

    private fun initView() {
        etGender      = findViewById(R.id.etGender)
        etCategory    = findViewById(R.id.etCategory)
        etMobile      = findViewById(R.id.etMobile)
        etParentEmail = findViewById(R.id.etParentEmail)
        etFatherName  = findViewById(R.id.etFatherName)
        etMotherName  = findViewById(R.id.etMotherName)

        etStreet = findViewById(R.id.etStreet)
        etCity   = findViewById(R.id.etCity)
        etState  = findViewById(R.id.etState)

        btnSaveChanges = findViewById(R.id.btnSaveChanges)
    }

    private fun setupActions() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnTopEdit).setOnClickListener {
            Toast.makeText(this, "Edit profile", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.avatarEditGroup).setOnClickListener {
            Toast.makeText(this, "Change photo", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.tabGeneral).setOnClickListener {
            switchTab(showGeneral = true)
        }

        findViewById<View>(R.id.tabLocation).setOnClickListener {
            switchTab(showGeneral = false)
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        switchTab(showGeneral = false)
    }

    private fun switchTab(showGeneral: Boolean) {
        isGeneralSelected = showGeneral

        val tabGeneral  = findViewById<TextView>(R.id.tabGeneral)
        val tabLocation = findViewById<TextView>(R.id.tabLocation)
        val formGeneral = findViewById<View>(R.id.formGeneral)
        val formLocation = findViewById<View>(R.id.locationForm)

        if (showGeneral) {
            tabGeneral.setBackgroundResource(R.drawable.bg_figma_tab_location_selected)
            tabGeneral.setTextColor(0xFFFFFFFF.toInt())
            tabLocation.setBackgroundResource(R.drawable.bg_figma_tab_general_unselected)
            tabLocation.setTextColor(0xFFADADAD.toInt())
            formGeneral.visibility  = View.VISIBLE
            formLocation.visibility = View.GONE
        } else {
            tabLocation.setBackgroundResource(R.drawable.bg_figma_tab_location_selected)
            tabLocation.setTextColor(0xFFFFFFFF.toInt())
            tabGeneral.setBackgroundResource(R.drawable.bg_figma_tab_general_unselected)
            tabGeneral.setTextColor(0xFFADADAD.toInt())
            formLocation.visibility = View.VISIBLE
            formGeneral.visibility  = View.GONE
        }
    }

    /**
     * 点击 Save Changes：
     * 1. 禁用按钮
     * 2. 收集字段 → 打印 Log
     * 3. Retrofit 发请求（MockInterceptor 拦截，不出网络）
     * 4. HTTP 200 → 成功弹窗；非 200 → 失败弹窗
     */
    private fun saveChanges() {
        // Step 1：禁用按钮，防止重复提交
        btnSaveChanges.isEnabled = false
        btnSaveChanges.text = "Saving..."

        // Step 2：收集表单数据
        val request = ProfileRequest(
            gender      = etGender.text.toString().trim(),
            category    = etCategory.text.toString().trim(),
            mobile      = etMobile.text.toString().trim(),
            parentEmail = etParentEmail.text.toString().trim(),
            fatherName  = etFatherName.text.toString().trim(),
            motherName  = etMotherName.text.toString().trim(),
            street      = etStreet.text.toString().trim(),
            city        = etCity.text.toString().trim(),
            state       = etState.text.toString().trim()
        )

        // 打印将要发送的请求体
        Log.d(TAG, "===== Save Profile Request Body =====")
        Log.d(TAG, "  gender       : ${request.gender}")
        Log.d(TAG, "  category     : ${request.category}")
        Log.d(TAG, "  mobile       : ${request.mobile}")
        Log.d(TAG, "  parentEmail  : ${request.parentEmail}")
        Log.d(TAG, "  fatherName   : ${request.fatherName}")
        Log.d(TAG, "  motherName   : ${request.motherName}")
        Log.d(TAG, "  street       : ${request.street}")
        Log.d(TAG, "  city         : ${request.city}")
        Log.d(TAG, "  state        : ${request.state}")
        Log.d(TAG, "=====================================")

        // Step 3：在 lifecycleScope 协程里发起请求
        // lifecycleScope：Activity 销毁时自动取消，不会内存泄漏
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.profileApiService.saveProfile(request)

                Log.d(TAG, "HTTP Response Code: ${response.code()}")

                // Step 4：根据状态码决定弹窗内容
                if (response.isSuccessful) {
                    // isSuccessful = code in [200..299]
                    showResultDialog(
                        title   = "Success",
                        message = "Profile saved successfully!\n(HTTP ${response.code()})"
                    )
                } else {
                    showResultDialog(
                        title   = "Failed",
                        message = "Save failed. Server returned HTTP ${response.code()}.\nPlease try again."
                    )
                }

            } catch (e: Exception) {
                // 网络异常（超时、无网络等）
                Log.e(TAG, "Network error: ${e.message}", e)
                showResultDialog(
                    title   = "Error",
                    message = "Network error: ${e.message}"
                )
            }
        }
    }

    /** 弹出结果弹窗，关闭后恢复按钮 */
    private fun showResultDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                btnSaveChanges.isEnabled = true
                btnSaveChanges.text = "Save changes"
            }
            .show()
    }
}
