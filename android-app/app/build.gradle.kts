plugins {
    id("com.android.application")
}

android {
    namespace = "com.what2eat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.what2eat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            // 真机测试：使用电脑的局域网IP（根据实际情况修改）
            // 请将下面的IP地址改为您电脑的局域网IP
            // buildConfigField("String", "BASE_URL", "\"http://10.88.1.127:8883/api/\"")
            // buildConfigField("String", "WS_URL", "\"ws://10.88.1.127:8883/api/ws\"")

            // 生产环境：使用域名（DNS-only模式，HTTP）
            buildConfigField("String", "BASE_URL", "\"http://api.jamesweb.org:8883/api/\"")
            buildConfigField("String", "WS_URL", "\"ws://api.jamesweb.org:8883/api/ws\"")

            // 模拟器测试：使用10.0.2.2
            // buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8883/api/\"")
            // buildConfigField("String", "WS_URL", "\"ws://10.0.2.2:8883/api/ws\"")
        }
        release {
            // 生产环境：使用域名（DNS-only模式，HTTP）
            buildConfigField("String", "BASE_URL", "\"http://api.jamesweb.org:8883/api/\"")
            buildConfigField("String", "WS_URL", "\"ws://api.jamesweb.org:8883/api/ws\"")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ZXing (二维码)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")

    // OkHttp WebSocket
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
