package com.nandaadisaputra.onlineshop.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiConfig adalah kelas Singleton (dideklarasikan menggunakan keyword `object`) yang bertugas
 * sebagai pusat konfigurasi koneksi jaringan (Networking Client) menggunakan library Retrofit dan OkHttp.
 *
 * Menggunakan Singleton memastikan bahwa instansiasi Retrofit dan OkHttpClient hanya dibuat sekali
 * selama aplikasi berjalan, sehingga menghemat memori dan performa sistem.
 */
object ApiConfig {
    // Alamat dasar (Base URL) dari server backend web service proyek ini
    private const val BASE_URL = "https://pasarrakyatbangetayukulon.my.id/"

    /**
     * 1. Versi TANPA Context (`provideApiService` sebagai Lazy Property)
     * Digunakan untuk endpoint publik yang tidak memerlukan autentikasi token (seperti proses Login, Register, atau umum)[cite: 4].
     * Keyword `by lazy` memastikan objek Retrofit ini baru akan dibuat (dikonversi ke memori) saat pertama kali dipanggil.
     */
    val provideApiService: ApiService by lazy {
        // Membuat HttpLoggingInterceptor untuk mencatat (log) seluruh aktivitas lalu lintas HTTP
        // (request header, body, response, hingga error code) ke Logcat Android Studio untuk keperluan debugging.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Menampilkan detail penuh (body & header)
        }

        // Membangun klien HTTP menggunakan OkHttpClient.Builder
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Menambahkan interceptor logging
            .connectTimeout(
                30,
                TimeUnit.SECONDS
            ) // Batas maksimal waktu tunggu koneksi ke server (30 detik)
            .readTimeout(
                30,
                TimeUnit.SECONDS
            )    // Batas maksimal waktu tunggu membaca data dari server (30 detik)
            .build()

        // Membangun instansiasi Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Menetapkan URL dasar server[cite: 4]
            .client(client)    // Menghubungkan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create()) // Menggunakan Gson untuk mengubah JSON otomatis menjadi Data Class Kotlin
            .build()
            .create(ApiService::class.java) // Menghasilkan implementasi dari interface ApiService
    }

    /**
     * 2. Versi DENGAN Context (`provideApiService(context)` berupa fungsi biasa)[cite: 4]
     * Digunakan untuk endpoint privat yang membutuhkan autentikasi token (seperti mengambil produk, keranjang, atau update profil)[cite: 4].
     *
     * @param context Context aplikasi atau aktivitas untuk membaca data token dari SharedPreferences melalui AuthInterceptor[cite: 4, 7].
     * @return Instance [ApiService] yang sudah dilengkapi dengan penyisipan Header Token otomatis[cite: 4, 7].
     */
    fun provideApiService(context: Context): ApiService {
        // Membuat interceptor logging untuk memantau request dan response di Logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Membangun klien OkHttpClient dengan menyisipkan dua interceptor khusus:
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // 1. AuthInterceptor: Berfungsi membaca token lokal dan menyisipkannya ke Header HTTP secara otomatis[cite: 4, 7]
            .addInterceptor(loggingInterceptor)       // 2. LoggingInterceptor: Mencatat log jaringan
            .connectTimeout(30, TimeUnit.SECONDS)     // Batas waktu koneksi (30 detik)
            .readTimeout(30, TimeUnit.SECONDS)        // Batas waktu baca data (30 detik)
            .build()

        // Membangun dan mengembalikan objek Retrofit baru yang sudah membawa AuthInterceptor
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}