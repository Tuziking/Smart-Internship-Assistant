plugins {
    id("com.android.application")
}

android {
    namespace = "com.B0BO.smart_internship_assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.B0BO.smart_internship_assistant"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("com.airbnb.android:lottie:6.4.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    //导入高德地图的SDK 切记不要盲目粘贴，可能出现一些问题，最好是用自己下载的aar包
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

//    implementation("com.amap.api:navi-3dmap:latest.integration")
//    implementation("com.amap.api:search:latest.integration")
//    implementation ("com.amap.api:search:latest.integration")
//    implementation ("com.amap.api:location:latest.integration")


//    implementation("com.tencent.map:tencent-map-nav-sdk-core:6.7.10")
//    implementation("com.tencent.openmap:foundation:6.7.10")
//    implementation("com.tencent.map:tencent-map-nav-sdk-tts:6.7.10")


    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}