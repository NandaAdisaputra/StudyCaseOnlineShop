package com.nandaadisaputra.onlineshop.ui.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToDashboard
import com.nandaadisaputra.onlineshop.data.helper.navigateToLogin
import com.nandaadisaputra.onlineshop.databinding.ActivitySplashBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.SplashDestination
import com.nandaadisaputra.onlineshop.ui.viewmodel.SplashViewModel
import kotlin.getValue

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeNavigation()
    }

    private fun observeNavigation() {
        viewModel.navigateTo.observe(this) { destination ->
            when (destination) {
                is SplashDestination.Dashboard -> {
                    // Arahkan ke dashboard sesuai role (Admin / User) dan tutup SplashActivity
                    navigateToDashboard(destination.role, finishCurrent = true)
                }
                is SplashDestination.Login -> {
                    // Arahkan ke LoginActivity dan tutup SplashActivity
                    navigateToLogin(finishCurrent = true)
                }
            }
        }
    }
}