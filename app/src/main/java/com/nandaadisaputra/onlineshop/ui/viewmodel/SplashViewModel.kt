package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * SplashViewModel adalah kelas pengelola logika tampilan (ViewModel dalam arsitektur MVVM)
 * untuk halaman SplashActivity.
 *
 * Kelas ini mewarisi [AndroidViewModel] karena membutuhkan objek [Application] (Application Context)
 * untuk menginisialisasi [UserPreference] guna memeriksa status sesi login pengguna di penyimpanan lokal.
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi UserPreference untuk membaca data sesi lokal (token, role, status login)
    private val userPreference = UserPreference(application)

    // =========================================================================================
    // 2. ENKAPSULASI LIVE DATA UNTUK NAVIGASI (Backing Property Pattern)
    // =========================================================================================

    // _navigateTo bersifat 'private' dan 'MutableLiveData' (bisa diubah nilainya).
    // Hanya ViewModel ini saja yang berhak mengubah atau memutuskan ke mana aplikasi harus bernavigasi.
    private val _navigateTo = MutableLiveData<SplashDestination>()

    // navigateTo bersifat 'public' dan bertipe 'LiveData' (Read-Only).
    // Variabel inilah yang diamati (di-observe) oleh SplashActivity untuk melakukan perpindahan halaman.
    val navigateTo: LiveData<SplashDestination> get() = _navigateTo

    /**
     * Blok 'init' (Inisialisasi) akan otomatis dijalankan begitu objek SplashViewModel pertama kali
     * dibuat di memori (saat SplashActivity dibuka).
     */
    init {
        checkSessionAndDelay()
    }

    /**
     * Fungsi privat untuk memeriksa status sesi login sekaligus memberikan jeda waktu (delay)
     * agar logo splash screen sempat terlihat dengan baik oleh pengguna.
     */
    private fun checkSessionAndDelay() {
        // Dijalankan di dalam 'viewModelScope.launch' (CoroutineScope bawaan ViewModel).
        // Proses ini berjalan secara asinkron di latar belakang tanpa membuat UI utama macet (freeze).
        viewModelScope.launch {
            // Memberikan jeda waktu (delay) selama 1500 milidetik (1.5 detik) menggunakan Kotlin Duration API
            delay(1500.milliseconds)

            // Memeriksa status login melalui UserPreference (SharedPreferences)
            if (userPreference.isLoggedIn()) {
                // Jika sudah login: Ambil data role (hak akses), berikan nilai default "user" jika kosong
                val role = userPreference.getRole() ?: "user"
                // Kirim perintah navigasi ke Dashboard dengan membawa data role
                _navigateTo.value = SplashDestination.Dashboard(role)
            } else {
                // Jika belum login: Kirim perintah navigasi ke halaman Login
                _navigateTo.value = SplashDestination.Login
            }
        }
    }
}

/**
 * SplashDestination adalah kelas berpola Sealed Class yang bertindak sebagai pembungkus tujuan navigasi
 * secara aman dan terstruktur (Type-safe).
 * Memastikan bahwa SplashActivity hanya dapat bernavigasi ke dua kemungkinan tujuan ini saja.
 */
sealed class SplashDestination {
    // Tujuan 1: Menuju ke halaman Dashboard (membawa informasi role: "admin" atau "user")
    data class Dashboard(val role: String) : SplashDestination()

    // Tujuan 2: Menuju ke halaman Login (tidak memerlukan parameter tambahan)
    object Login : SplashDestination()
}