package com.nandaadisaputra.onlineshop.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.loadFragment // Mengimpor extension function helper untuk mempermudah penggantian fragment
import com.nandaadisaputra.onlineshop.databinding.ActivityMainBinding
import com.nandaadisaputra.onlineshop.ui.fragment.CartFragment
import com.nandaadisaputra.onlineshop.ui.fragment.HomeFragment
import com.nandaadisaputra.onlineshop.ui.fragment.ProfileFragment

/**
 * MainActivity adalah halaman utama berbasis Activity yang bertindak sebagai "Host" (wadah)
 * bagi beberapa halaman berbentuk Fragment (HomeFragment, CartFragment, dan ProfileFragment).
 *
 * Activity ini dilengkapi dengan komponen Bottom Navigation (navigasi bawah) agar pengguna
 * dapat dengan mudah berpindah antar menu utama di dalam satu layar tanpa harus membuat Activity baru.
 */
class MainActivity : AppCompatActivity() {

    // 1. Deklarasi ViewBinding untuk mengakses komponen layout (activity_main.xml)
    // secara aman tanpa perlu menggunakan findViewById().
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML activity_main.xml menjadi objek View di memori
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // 2. PENGECEKAN SAVED INSTANCE STATE
        // 'if (savedInstanceState == null)' sangat penting!
        // Tujuannya agar saat Activity dibuat ulang (misalnya karena pengguna memutar posisi layar / rotasi HP),
        // fragment awal tidak dimuat ulang dan menumpuk di atas fragment yang sudah ada (mencegah bug tumpang tindih UI).
        if (savedInstanceState == null) {
            // Memuat HomeFragment sebagai tampilan default pertama kali saat aplikasi dibuka
            loadFragment(R.id.fragment_container, HomeFragment())
        }

        // 3. LISTENER BOTTOM NAVIGATION
        // Mengatur aksi ketika item/menu di bilah navigasi bawah diklik oleh pengguna.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Kasus 1: Menu Home diklik -> Ganti fragment container dengan HomeFragment
                R.id.nav_home -> {
                    loadFragment(R.id.fragment_container, HomeFragment())
                    true
                }

                // Kasus 2: Menu Keranjang / Cart diklik -> Ganti fragment container dengan CartFragment
                R.id.nav_cart -> {
                    loadFragment(R.id.fragment_container, CartFragment())
                    true
                }

                // Kasus 3: Menu Profil diklik -> Ganti fragment container dengan ProfileFragment
                R.id.nav_profile -> {
                    loadFragment(R.id.fragment_container, ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }
}