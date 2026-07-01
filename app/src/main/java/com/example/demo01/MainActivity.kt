package com.example.demo01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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
    private lateinit var rvList: androidx.recyclerview.widget.RecyclerView
    private lateinit var textAdapter: MyTextAdapter

    // 用来演示状态恢复的计数器
    private var clickCount = 0

    // 注册页面跳转结果回调
    private val jumpLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val backData = result.data?.getStringExtra("back_key")
                Toast.makeText(this, "从第二页回传：$backData", Toast.LENGTH_SHORT).show()
            }
        }

    // 注册 Bundle 跳转结果回调
    private val bundleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val backData = result.data?.getStringExtra("back_key")
                Toast.makeText(this, "从第二页(Bundle)回传：$backData", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initClickEvent()

        // ★ 用法②：从 savedInstanceState 恢复数据
        // 如果 Activity 因屏幕旋转等被系统重建，savedInstanceState 不为 null
        if (savedInstanceState != null) {
            clickCount = savedInstanceState.getInt("click_count", 0)
            Toast.makeText(this, "恢复了点击次数：$clickCount", Toast.LENGTH_SHORT).show()
        }
    }

    // ★ 用法②：保存状态 —— 屏幕旋转、系统回收内存时调用
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("click_count", clickCount)
        // 可以存任何基本类型：String, Int, Float, Boolean, Long 等
    }

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
        btnProfile   = findViewById(R.id.btn_profile)

        rvList = findViewById(R.id.rv_list)
        val initData = mutableListOf<String>()
        textAdapter = MyTextAdapter(initData)
        rvList.adapter = textAdapter
        rvList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val testData = listOf("测试数据1", "测试数据2", "测试数据3", "后端接口模拟数据")
        textAdapter.refreshData(testData)
    }

    private fun initClickEvent() {
        btnShow.setOnClickListener {
            val text = etInput.text.toString().trim()
            clickCount++
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "输入内容：$text (点击次数：$clickCount)", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnClear.setOnClickListener {
            etInput.setText("")
        }

        // 旧的跳转方式：逐个 putExtra（底层还是 Bundle，只是包装了）
        btnJump.setOnClickListener {
            val text = etInput.text.toString().trim()
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("send_key", text)
            jumpLauncher.launch(intent)
        }

        // ★ 用法①：显式创建 Bundle，一次性打包多个值
        btnBundle.setOnClickListener {
            val text = etInput.text.toString().trim()
            val bundle = Bundle()
            bundle.putString("send_key", text)          // String 类型
            bundle.putInt("user_id", 10086)             // Int 类型
            bundle.putBoolean("is_vip", true)            // Boolean 类型
            bundle.putDouble("score", 95.5)              // Double 类型

            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtras(bundle)  // 把整个 Bundle 放进 Intent
            bundleLauncher.launch(intent)
        }

        // ★ Fragment 演示跳转
        btnFragment.setOnClickListener {
            val intent = Intent(this, FragmentDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ ViewPager2 演示跳转
        btnViewPager.setOnClickListener {
            val intent = Intent(this, ViewPagerDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ 网络请求演示跳转
        btnNetwork.setOnClickListener {
            val intent = Intent(this, NetworkDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ ViewModel + LiveData 演示跳转
        btnViewModel.setOnClickListener {
            val intent = Intent(this, ViewModelDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ LiveData 对比实验跳转
        btnLiveDataCompare.setOnClickListener {
            val intent = Intent(this, LiveDataCompareActivity::class.java)
            startActivity(intent)
        }

        // ★ MVP 架构演示跳转
        btnMvp.setOnClickListener {
            val intent = Intent(this, MvpDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ DeepLink 演示跳转 —— 用隐式 Intent 模拟外部触发
        //
        // 真实场景：用户在微信/浏览器/Push 里点了一个链接
        //           Android 系统查路由表，找到我们 App 能处理这个 URL
        //           系统启动 DeepLinkDemoActivity，intent.data = 这个 URI
        //
        // 这里模拟：App 内部构造同样的隐式 Intent，效果等价
        btnDeepLink.setOnClickListener {
            val uri = android.net.Uri.parse("demo01://post?id=888&title=DeepLink学习Demo")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // ★ AMS 信息展示跳转
        btnAms.setOnClickListener {
            val intent = Intent(this, AmsDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ Service 演示跳转
        btnService.setOnClickListener {
            val intent = Intent(this, ServiceDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ Handler/Looper 演示跳转
        btnHandler.setOnClickListener {
            val intent = Intent(this, HandlerDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ SurfaceFlinger 演示跳转
        btnSurface.setOnClickListener {
            val intent = Intent(this, SurfaceDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ Broadcast 演示跳转
        btnBroadcast.setOnClickListener {
            val intent = Intent(this, BroadcastDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ ContentProvider 演示跳转
        btnContentProvider.setOnClickListener {
            val intent = Intent(this, ContentProviderDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ WebView 演示跳转
        btnWebview.setOnClickListener {
            val intent = Intent(this, WebViewDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ 离线包演示跳转
        btnOfflineDemo.setOnClickListener {
            val intent = Intent(this, OfflineDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ 热更新演示跳转
        btnHotUpdate.setOnClickListener {
            val intent = Intent(this, HotUpdateDemoActivity::class.java)
            startActivity(intent)
        }

        // ★ 个人资料页（Figma UI 练习）
        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // ★ RN URI 路由：三个页面入口
        fun openRN(uri: String) {
            val intent = Intent(this, RNContainerActivity::class.java)
            intent.putExtra("uri", uri)
            startActivity(intent)
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