package com.example.demo01

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.core.BannerHost
import com.example.demo.AmsDemoActivity
import com.example.demo.BroadcastDemoActivity
import com.example.demo.ContentProviderDemoActivity
import com.example.demo.DeepLinkDemoActivity
import com.example.demo.DiffUtilDemoActivity
import com.example.demo.FragmentDemoActivity
import com.example.demo.HandlerDemoActivity
import com.example.demo.LiveDataCompareActivity
import com.example.demo.MvpDemoActivity
import com.example.demo.MyTextAdapter
import com.example.demo.NetworkDemoActivity
import com.example.demo.ProfileActivity
import com.example.demo.SecondActivity
import com.example.demo.ServiceDemoActivity
import com.example.demo.SurfaceDemoActivity
import com.example.demo.ViewModelDemoActivity
import com.example.demo.ViewPagerDemoActivity
import com.example.demo.WanasActivity
import com.example.demo.WebViewDemoActivity
import com.example.krn.ActivityHolder
import com.example.krn.HotUpdateDemoActivity
import com.example.krn.KRNActivity
import com.example.krn.OfflineDemoActivity
import com.example.krn.RNContainerActivity

/**
 * MainActivity：App 入口，连接所有功能模块
 *
 * 实现 BannerHost 接口，让 feature-krn 里的 NativeBannerModule
 * 可以通过接口操作本页面的 View，而不需要直接依赖 MainActivity 类
 */
class MainActivity : AppCompatActivity(), BannerHost {

    private lateinit var etInput: EditText
    private lateinit var btnShow: Button
    private lateinit var btnClear: Button
    private lateinit var btnJump: Button
    private lateinit var btnDialog: Button
    private lateinit var btnBundle: Button
    private lateinit var btnFragment: Button
    private lateinit var btnViewPager: Button
    private lateinit var btnNetwork: Button
    private lateinit var btnViewModel: Button
    private lateinit var btnLiveDataCompare: Button
    private lateinit var btnMvp: Button
    private lateinit var btnDeepLink: Button
    private lateinit var btnAms: Button
    private lateinit var btnService: Button
    private lateinit var btnHandler: Button
    private lateinit var btnSurface: Button
    private lateinit var btnBroadcast: Button
    private lateinit var btnContentProvider: Button
    private lateinit var btnWebview: Button
    private lateinit var btnOfflineDemo: Button
    private lateinit var btnHotUpdate: Button
    private lateinit var btnProfile: Button
    private lateinit var btnWanas: Button
    private lateinit var rvList: androidx.recyclerview.widget.RecyclerView
    private lateinit var textAdapter: MyTextAdapter

    private var clickCount = 0

    private val jumpLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val backData = result.data?.getStringExtra("back_key")
                Toast.makeText(this, "从第二页回传：$backData", Toast.LENGTH_SHORT).show()
            }
        }

    private val bundleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val backData = result.data?.getStringExtra("back_key")
                Toast.makeText(this, "从第二页(Bundle)回传：$backData", Toast.LENGTH_SHORT).show()
            }
        }

    // ─── BannerHost 接口实现 ───────────────────────────────────────────────────

    /** NativeBannerModule 通过接口来 find View，不需要直接持有 MainActivity */
    override fun findViewByResId(id: Int): View? = findViewById(id)

    /** NativeBannerModule 通过接口切到主线程，不需要直接持有 MainActivity */
    override fun runOnMainThread(action: () -> Unit) = runOnUiThread(action)

    // ─── 生命周期 ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initClickEvent()

        // ★ 注册到 ActivityHolder，供 NativeBannerModule 通过 Bridge 控制本页 Banner
        ActivityHolder.host = this

        if (savedInstanceState != null) {
            clickCount = savedInstanceState.getInt("click_count", 0)
            Toast.makeText(this, "恢复了点击次数：$clickCount", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.host = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("click_count", clickCount)
    }

    // ─── 初始化 ───────────────────────────────────────────────────────────────

    private fun initView() {
        etInput = findViewById(R.id.et_input)
        btnShow = findViewById(R.id.btn_show)
        btnClear = findViewById(R.id.btn_clear)
        btnJump = findViewById(R.id.btn_jump)
        btnDialog = findViewById(R.id.btn_dialog)
        btnBundle = findViewById(R.id.btn_bundle)
        btnFragment = findViewById(R.id.btn_fragment)
        btnViewPager = findViewById(R.id.btn_viewpager)
        btnNetwork = findViewById(R.id.btn_network)
        btnViewModel = findViewById(R.id.btn_viewmodel)
        btnLiveDataCompare = findViewById(R.id.btn_livedata_compare)
        btnMvp = findViewById(R.id.btn_mvp)
        btnDeepLink = findViewById(R.id.btn_deeplink)
        btnAms = findViewById(R.id.btn_ams)
        btnService = findViewById(R.id.btn_service)
        btnHandler = findViewById(R.id.btn_handler)
        btnSurface = findViewById(R.id.btn_surface)
        btnBroadcast = findViewById(R.id.btn_broadcast)
        btnContentProvider = findViewById(R.id.btn_content_provider)
        btnWebview = findViewById(R.id.btn_webview)
        btnOfflineDemo = findViewById(R.id.btn_offline_demo)
        btnHotUpdate = findViewById(R.id.btn_hot_update)
        btnProfile = findViewById(R.id.btn_profile)
        btnWanas = findViewById(R.id.btn_wanas)

        rvList = findViewById(R.id.rv_list)
        val initData = mutableListOf<String>()
        textAdapter = MyTextAdapter(initData)
        rvList.adapter = textAdapter
        rvList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        textAdapter.refreshData(listOf("测试数据1", "测试数据2", "测试数据3", "后端接口模拟数据"))
    }

    private fun initClickEvent() {
        btnShow.setOnClickListener {
            val text = etInput.text.toString().trim()
            clickCount++
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "输入内容：$text (点击次数：$clickCount)", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener { etInput.setText("") }

        btnJump.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("send_key", etInput.text.toString().trim())
            jumpLauncher.launch(intent)
        }

        btnBundle.setOnClickListener {
            val bundle = Bundle().apply {
                putString("send_key", etInput.text.toString().trim())
                putInt("user_id", 10086)
                putBoolean("is_vip", true)
                putDouble("score", 95.5)
            }
            bundleLauncher.launch(Intent(this, SecondActivity::class.java).apply { putExtras(bundle) })
        }

        btnFragment.setOnClickListener { startActivity(Intent(this, FragmentDemoActivity::class.java)) }
        btnViewPager.setOnClickListener { startActivity(Intent(this, ViewPagerDemoActivity::class.java)) }
        btnNetwork.setOnClickListener { startActivity(Intent(this, NetworkDemoActivity::class.java)) }
        btnViewModel.setOnClickListener { startActivity(Intent(this, ViewModelDemoActivity::class.java)) }
        btnLiveDataCompare.setOnClickListener { startActivity(Intent(this, LiveDataCompareActivity::class.java)) }
        btnMvp.setOnClickListener { startActivity(Intent(this, MvpDemoActivity::class.java)) }

        btnDeepLink.setOnClickListener {
            val uri = android.net.Uri.parse("demo01://post?id=888&title=DeepLink学习Demo")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        btnAms.setOnClickListener { startActivity(Intent(this, AmsDemoActivity::class.java)) }
        btnService.setOnClickListener { startActivity(Intent(this, ServiceDemoActivity::class.java)) }
        btnHandler.setOnClickListener { startActivity(Intent(this, HandlerDemoActivity::class.java)) }
        btnSurface.setOnClickListener { startActivity(Intent(this, SurfaceDemoActivity::class.java)) }
        btnBroadcast.setOnClickListener { startActivity(Intent(this, BroadcastDemoActivity::class.java)) }
        btnContentProvider.setOnClickListener { startActivity(Intent(this, ContentProviderDemoActivity::class.java)) }
        btnWebview.setOnClickListener { startActivity(Intent(this, WebViewDemoActivity::class.java)) }
        btnOfflineDemo.setOnClickListener { startActivity(Intent(this, OfflineDemoActivity::class.java)) }
        btnHotUpdate.setOnClickListener { startActivity(Intent(this, HotUpdateDemoActivity::class.java)) }
        btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        btnWanas.setOnClickListener { startActivity(Intent(this, WanasActivity::class.java)) }

        findViewById<Button>(R.id.btn_diffutil).setOnClickListener {
            startActivity(Intent(this, DiffUtilDemoActivity::class.java))
        }

        // ★★★ KRN 演示（真实 React Native + Bridge）
        findViewById<Button>(R.id.btn_krn).setOnClickListener {
            startActivity(Intent(this, KRNActivity::class.java))
        }

        // ★ RN URI 路由
        fun openRN(uri: String) {
            startActivity(Intent(this, RNContainerActivity::class.java).apply {
                putExtra("uri", uri)
            })
        }
        findViewById<Button>(R.id.btn_rn_product).setOnClickListener {
            openRN("myapp://product/detail?id=666&name=%E8%93%9D%E7%89%99%E8%80%B3%E6%9C%BA")
        }
        findViewById<Button>(R.id.btn_rn_user).setOnClickListener {
            openRN("myapp://user/profile?userId=u_10086")
        }
        findViewById<Button>(R.id.btn_rn_order).setOnClickListener {
            openRN("myapp://order/list")
        }

        btnDialog.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("操作确认")
                .setMessage("确定要执行该操作吗？")
                .setPositiveButton("确定") { _, _ ->
                    Toast.makeText(this, "点击了确定", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}
