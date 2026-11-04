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
    // Core Android & UI Components (Bawaan Proyek)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Networking & API Parsing (Retrofit & Gson Converter untuk komunikasi data dengan Server/REST API)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Lifecycle Architecture Components (ViewModel untuk manajemen state UI & LiveData untuk observasi data reaktif)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // MVVM & Asynchronous Programming (Activity KTX untuk ekstensi Activity & Coroutines Android untuk proses background asinkron)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.coroutines.android)

    // Fragment KTX (Ekstensi Kotlin untuk mempermudah manipulasi Fragment dan ViewModel scoping)
    implementation(libs.androidx.fragment.ktx)

    // Image Loader & Network Logging (Glide untuk memuat gambar dari URL, OkHttp & Logging Interceptor untuk debugging HTTP request)
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}