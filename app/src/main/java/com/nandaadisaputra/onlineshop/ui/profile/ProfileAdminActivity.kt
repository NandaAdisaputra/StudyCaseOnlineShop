package com.nandaadisaputra.onlineshop.ui.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToLoginOnLogout
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.ActivityProfileAdminBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AuthViewModel

class ProfileAdminActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================
    private lateinit var binding: ActivityProfileAdminBinding
    private val authViewModel: AuthViewModel by viewModels() // Inisialisasi ViewModel
    private lateinit var userPreference: UserPreference       // Untuk mengelola sesi SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginisialisasi View Binding
        binding = ActivityProfileAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(this)

        setupView()
        setupListeners()
        observeLogoutResult()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN & EVENT HANDLING
    // ====================================================================
    private fun setupView() {
        // Tampilkan username admin yang tersimpan di SharedPreferences
        val username = userPreference.getUsername()
        binding.tvUsername.text = username ?: "Administrator"
    }

    private fun setupListeners() {
        // Aksi ketika tombol Logout diklik
        binding.btnLogout.setOnClickListener {
            val token = userPreference.getToken()
            if (!token.isNullOrEmpty()) {
                // Panggil API logout server dengan membawa token aktif
                authViewModel.logout(token)
            } else {
                // Jika token tidak ada, langsung bersihkan sesi lokal
                performLocalLogout()
            }
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================
    private fun observeLogoutResult() {
        authViewModel.logoutResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    showToast(result.data.message ?: "Logout berhasil")
                    performLocalLogout()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    // Tetap lakukan logout lokal meskipun server mengembalikan error (misal 401 Unauthorized)
                    showToast(result.message)
                    performLocalLogout()
                }
            }
        }
    }
    /**
     * Mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan tombol Logout menggunakan extension helper.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(isLoading, binding.btnLogout)
    }

    /**
     * Menghapus sesi lokal dan kembali ke LoginActivity secara bersih menggunakan extension helper.
     */
    private fun performLocalLogout() {
        // Menghapus data sesi SharedPreferences
        userPreference.logout()

        // Pindah kembali ke halaman Login dan hapus activity stack menggunakan helper
        navigateToLoginOnLogout()
    }
}