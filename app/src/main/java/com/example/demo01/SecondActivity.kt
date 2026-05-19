package com.example.demo01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    private lateinit var tvMsg: TextView
    private lateinit var btnBack: Button
    private lateinit var tvBundleInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        initView()
        receiveData()
        receiveBundleData()
        initBackClick()
    }

    private fun initView(){
        tvMsg = findViewById(R.id.tv_receive_msg)
        btnBack = findViewById(R.id.btn_back)
        tvBundleInfo = findViewById(R.id.tv_bundle_info)
    }

    private fun receiveData(){
        val content = intent.getStringExtra("send_key")
        if(!content.isNullOrEmpty()){
            tvMsg.text = "收到首页数据：$content"
        }
    }

    // ★ 接收 Bundle 方式传来的多个值
    private fun receiveBundleData(){
        val bundle = intent.extras  // 取出整个 Bundle
        if (bundle != null) {
            val content = bundle.getString("send_key", "")
            val userId = bundle.getInt("user_id", 0)
            val isVip = bundle.getBoolean("is_vip", false)
            val score = bundle.getDouble("score", 0.0)
            tvBundleInfo.text = "Bundle数据：\n内容=$content\n用户ID=$userId\nVIP=$isVip\n分数=$score"
        }
    }

    private fun initBackClick(){
        btnBack.setOnClickListener {
            // 封装回传数据
            val backIntent = Intent()
            backIntent.putExtra("back_key","我是第二页加工后的数据")
            setResult(RESULT_OK,backIntent)
            finish()
        }
    }
}