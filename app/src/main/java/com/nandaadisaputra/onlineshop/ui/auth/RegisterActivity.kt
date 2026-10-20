package com.nandaadisaputra.onlineshop.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToLogin
import com.nandaadisaputra.onlineshop.data.helper.setLoadingRegister
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityRegisterBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels() // Inisialisasi ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginisialisasi View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeRegisterResult()
    }

    // ====================================================================
    // 2. EVENT HANDLING (AKSI TOMBOL & INTERAKSI PENGGUNA)
    // ====================================================================
    private fun setupListeners() {
        // Aksi ketika tombol Register diklik
        binding.btnRegister.setOnClickListener {
            val username = binding.etRegisterUsername.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val alamat = binding.etRegisterAddress.text.toString().trim()
            val noWhatsApp = binding.etRegisterWhatsapp.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            // Validasi sederhana agar tidak ada kolom yang kosong
            if (username.isNotEmpty() && email.isNotEmpty() && alamat.isNotEmpty() && noWhatsApp.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(username, email, alamat, noWhatsApp, password)
            } else {
                showToast("Semua kolom harus diisi!")
            }
        }

        // Aksi ketika teks "Login disini" diklik
        binding.tvLoginAction.setOnClickListener {
            // Memanggil helper intent yang sudah dibuat
            navigateToLogin()
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================
    private fun observeRegisterResult() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    showToast(result.data.message ?: "Registrasi berhasil")

                    // Menggunakan Intent eksplisit kembali ke LoginActivity saat sukses
                    navigateToLogin()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showToast(result.message)
                }
            }
        }
    }

    // ====================================================================
    // 4. FUNGSI BANTUAN (HELPER METHODS)
    // ====================================================================

    /**
     * Mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan tombol Register
     */
    private fun showLoading(isLoading: Boolean) {
        // Cukup panggil extension function dengan menyertakan tombol register
        binding.progressBar.setLoadingRegister(
            isLoading,
            binding.btnRegister,
            binding.etRegisterEmail, // Opsional: jika form input ingin ikut dikunci saat loading
            binding.etRegisterAddress,
            binding.etRegisterUsername,
            binding.etRegisterWhatsapp,
            binding.tilRegisterPassword
        )
    }
}