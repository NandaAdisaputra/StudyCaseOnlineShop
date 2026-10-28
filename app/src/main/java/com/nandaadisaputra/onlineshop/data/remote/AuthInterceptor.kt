package com.nandaadisaputra.onlineshop.data.remote

import android.content.Context
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor adalah kelas yang mengimplementasikan OkHttp Interceptor.
 * Berfungsi sebagai "satpam" (middleware) untuk menyisipkan Token Autentikasi (Bearer Token)
 * secara otomatis ke setiap HTTP Request yang dikirimkan ke server tanpa harus menulisnya secara manual di setiap endpoint API.
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Menginisialisasi UserPreference untuk membaca sesi login dan token pengguna yang tersimpan di perangkat lokal
        val userPreference = UserPreference(context)
        val token = userPreference.getToken()

        // Mengambil request asli yang akan dikirim, lalu membuat builder-nya untuk dimodifikasi
        val requestBuilder = chain.request().newBuilder()

        // Jika token ditemukan (tidak null atau kosong), tambahkan ke Header HTTP dengan format "Bearer <token>"
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Melanjutkan rantai eksekusi (chain) dengan mengirimkan request baru yang sudah disisipi header otorisasi
        return chain.proceed(requestBuilder.build())
    }
}