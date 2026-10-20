package com.nandaadisaputra.onlineshop.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToDashboard
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.ActivityLoginBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(this)

        // ====================================================================
        // 2. PENGECEKAN SESI LOGIN OTOMATIS (AUTO-LOGIN)
        // ====================================================================
        if (userPreference.isLoggedIn()) {
            navigateToDashboard(userPreference.getRole() ?: "user")
            return
        }

        setupListeners()
        observeLoginResult()
    }

    // ====================================================================
    // 3. EVENT HANDLING (AKSI TOMBOL & INTERAKSI PENGGUNA)
    // ====================================================================
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            } else {
                showToast("Username dan password tidak boleh kosong")
            }
        }

        binding.tvRegisterAction.setOnClickListener {
            navigateToActivity<RegisterActivity>()
        }
    }

    // ====================================================================
    // 4. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)

                    val responseData = result.data.data
                    val token = responseData?.token
                    val username = responseData?.username
                    val role = responseData?.role ?: "user"

                    if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
                        userPreference.saveSession(token, username, role)
                        result.data.message?.let { showToast(it) }
                        navigateToDashboard(role)
                    } else {
                        showToast("Token atau data user tidak valid")
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showToast(result.message)
                }
            }
        }
    }

    /**
     * Mengatur visibilitas ProgressBar dan mengunci/membuka tombol serta input form saat loading.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(
            isLoading,
            binding.btnLogin,
            binding.etUsername,
            binding.etPassword,
            binding.tilUsername,
            binding.tilPassword
        )
    }
}