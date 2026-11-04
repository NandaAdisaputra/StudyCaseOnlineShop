package com.nandaadisaputra.onlineshop.ui.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToDashboard
import com.nandaadisaputra.onlineshop.data.helper.navigateToLogin
import com.nandaadisaputra.onlineshop.databinding.ActivitySplashBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.SplashDestination
import com.nandaadisaputra.onlineshop.ui.viewmodel.SplashViewModel

/**
 * SplashActivity adalah halaman pembuka (Splash Screen) saat aplikasi pertama kali dijalankan.
 * Halaman ini bertugas memeriksa status sesi pengguna di latar belakang (apakah sudah login atau belum)
 * melalui SplashViewModel, lalu secara otomatis mengarahkan (redirect) pengguna ke halaman Dashboard
 * atau halaman Login tanpa membuat UI utama menjadi macet.
 */
class SplashActivity : AppCompatActivity() {

    // 1. Deklarasi ViewBinding untuk mengakses komponen layout XML (activity_splash.xml)
    // secara aman tanpa perlu menggunakan findViewById().
    private lateinit var binding: ActivitySplashBinding

    // 2. Inisialisasi SplashViewModel menggunakan delegasi 'by viewModels()'.
    // Delegasi ini membuat instansiasi ViewModel dikelola secara otomatis oleh siklus hidup (Lifecycle) Activity,
    // sehingga data di dalam ViewModel tidak akan hilang saat terjadi rotasi layar.
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivitySplashBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi untuk mengamati (observe) perintah navigasi dari ViewModel
        observeNavigation()
    }

    /**
     * Fungsi untuk mengamati perubahan data (LiveData) yang dikirim oleh SplashViewModel.
     * Ketika ViewModel selesai melakukan pengecekan sesi di latar belakang, ia akan memancarkan
     * tujuan navigasi (SplashDestination), dan Activity akan meresponsnya secara reaktif.
     */
    private fun observeNavigation() {
        // Mengamati LiveData 'navigateTo' di dalam SplashViewModel
        viewModel.navigateTo.observe(this) { destination ->
            // Menggunakan ekspresi 'when' (pola percabangan ala Kotlin) untuk menentukan tujuan berdasarkan tipe datanya
            when (destination) {
                // Kondisi 1: Jika pengguna sudah login sebelumnya
                is SplashDestination.Dashboard -> {
                    // Arahkan ke dashboard yang sesuai dengan hak akses (Admin / User)
                    // dan tutup SplashActivity (finishCurrent = true) agar pengguna tidak bisa menekan tombol back kembali ke splash screen.
                    navigateToDashboard(destination.role, finishCurrent = true)
                }

                // Kondisi 2: Jika sesi belum ada atau pengguna belum login
                is SplashDestination.Login -> {
                    // Arahkan ke halaman LoginActivity dan tutup SplashActivity.
                    navigateToLogin(finishCurrent = true)
                }
            }
        }
    }
}