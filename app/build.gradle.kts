plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)   // [BARU DITAMBAHKAN]
    alias(libs.plugins.kotlin.parcelize) // [BARU DITAMBAHKAN]
}

android {
    namespace = "com.nandaadisaputra.onlineshop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nandaadisaputra.onlineshop"
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
    kotlinOptions { // [BARU DITAMBAHKAN]
        jvmTarget = "11" // [BARU DITAMBAHKAN]
    } // [BARU DITAMBAHKAN]

    buildFeatures { // [BARU DITAMBAHKAN] Sangat penting untuk ViewBinding
        viewBinding = true // [BARU DITAMBAHKAN]
        buildConfig = true // [BARU DITAMBAHKAN]
    } // [BARU DITAMBAHKAN]
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Networking & Parsing
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Lifecycle (ViewModel & LiveData)
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // [BARU DITAMBAHKAN]
    implementation(libs.androidx.lifecycle.livedata.ktx) // [BARU DITAMBAHKAN]

    // MVVM & Coroutines
    implementation(libs.androidx.activity.ktx)
    implementation(libs.coroutines.android)

    implementation(libs.androidx.fragment.ktx)

    // Image Loader
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
}