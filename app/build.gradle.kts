plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.demo01"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.demo01"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 列表控件
    implementation(libs.androidx.recyclerview)
    // ★ ViewPager2 + Fragment 滑动切换页面
    implementation(libs.androidx.viewpager2)

    // ★ 网络请求：Retrofit + OkHttp + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // ★ Kotlin 协程（异步编程，网络请求必须在子线程）
    implementation(libs.kotlinx.coroutines.android)

    // ★ Lifecycle KTX（提供 lifecycleScope，在 Activity 里启动协程）
    implementation(libs.lifecycle.runtime.ktx)

    // ★ ViewModel + LiveData（数据管理与生命周期感知）
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)

    // ★ LocalBroadcastManager（App 内部广播，不经过 AMS）
    implementation(libs.localbroadcastmanager)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}