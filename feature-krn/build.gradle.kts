plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.krn"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES", "META-INF/LICENSE",
                "META-INF/LICENSE.txt", "META-INF/license.txt",
                "META-INF/NOTICE", "META-INF/NOTICE.txt",
                "META-INF/notice.txt", "META-INF/*.kotlin_module"
            )
            pickFirsts += listOf(
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/arm64-v8a/libc++_shared.so"
            )
        }
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    // api: 让依赖本 module 的上层也能使用这些类（MainApplication 需要用）
    api("com.facebook.react:react-android:0.73.6")
    api("com.facebook.react:hermes-android:0.73.6")
    implementation(project(":core"))
}
