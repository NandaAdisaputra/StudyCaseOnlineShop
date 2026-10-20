package com.nandaadisaputra.onlineshop.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity // Import helper navigasi
import com.nandaadisaputra.onlineshop.databinding.ActivityAdminDashboardBinding
import com.nandaadisaputra.onlineshop.ui.profile.ProfileAdminActivity

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Navigasi ke halaman Profil Admin menggunakan helper generic activity
        binding.btnProfile.setOnClickListener {
            navigateToActivity<ProfileAdminActivity>()
        }
    }
}