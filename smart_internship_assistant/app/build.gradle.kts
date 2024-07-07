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
//    implementation("com.github.Wynsbin:VerificationCodeInputView:1.0.2")
//    implementation("com.makeramen:roundedimageview:2.3.0")
//    implementation ("com.github.bumptech.glide:glide:4.13.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
//    implementation(file("libs/BaiduLBS_Android.aar"))


    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}