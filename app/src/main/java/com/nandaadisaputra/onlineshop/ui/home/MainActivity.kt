package com.nandaadisaputra.onlineshop.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.loadFragment // Import helper yang dibuat
import com.nandaadisaputra.onlineshop.databinding.ActivityMainBinding
import com.nandaadisaputra.onlineshop.ui.fragment.CartFragment
import com.nandaadisaputra.onlineshop.ui.fragment.HomeFragment
import com.nandaadisaputra.onlineshop.ui.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment ke Home saat pertama kali dibuka
        if (savedInstanceState == null) {
            loadFragment(R.id.fragment_container, HomeFragment())
        }

        // Listener perpindahan menu Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(R.id.fragment_container, HomeFragment())
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(R.id.fragment_container, CartFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(R.id.fragment_container, ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
}