package com.nandaadisaputra.onlineshop.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity // Mengimpor extension helper generik untuk mempermudah perpindahan antar Activity
import com.nandaadisaputra.onlineshop.databinding.ActivityAdminDashboardBinding
import com.nandaadisaputra.onlineshop.ui.profile.ProfileAdminActivity

/**
 * AdminDashboardActivity adalah halaman utama (View berbasis Activity) khusus untuk hak akses Administrator.
 * Halaman ini berfungsi sebagai pusat navigasi bagi admin untuk memantau menu atau beralih
 * ke halaman pengelolaan profil admin (ProfileAdminActivity).
 */
class AdminDashboardActivity : AppCompatActivity() {

    // 1. Deklarasi ViewBinding untuk mengakses komponen layout XML (activity_admin_dashboard.xml)
    // secara aman tanpa harus menggunakan fungsi findViewById().
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi untuk mengatur interaksi tombol (event listeners)
        setupListeners()
    }

    /**
     * Fungsi untuk mengatur aksi klik (Event Listener) pada komponen-komponen UI di dashboard admin.
     */
    private fun setupListeners() {
        // Aksi ketika tombol Profil Admin (btn_profile) diklik oleh pengguna
        binding.btnProfile.setOnClickListener {
            // Menggunakan extension helper generik 'navigateToActivity<ProfileAdminActivity>()'
            // untuk berpindah dari Dashboard Admin ke halaman ProfileAdminActivity dengan ringkas dan bersih.
            navigateToActivity<ProfileAdminActivity>()
        }
    }
}