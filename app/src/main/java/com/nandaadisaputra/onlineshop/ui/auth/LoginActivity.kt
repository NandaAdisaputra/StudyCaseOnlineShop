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

/**
 * LoginActivity adalah halaman (View dalam arsitektur MVVM) yang bertugas menangani proses
 * masuk akun (Login) pengguna ke aplikasi, pengecekan auto-login, serta navigasi menuju halaman Dashboard
 * atau halaman Register.
 */
class LoginActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_login.xml secara aman
    private lateinit var binding: ActivityLoginBinding

    // Inisialisasi AuthViewModel menggunakan delegasi 'by viewModels()' untuk memproses logika login
    private val viewModel: AuthViewModel by viewModels()

    // Deklarasi UserPreference untuk menyimpan dan memeriksa sesi lokal di SharedPreferences
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityLoginBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Menginisialisasi objek UserPreference dengan Context Activity ini
        userPreference = UserPreference(this)

        // ====================================================================
        // 2. PENGECEKAN SESI LOGIN OTOMATIS (AUTO-LOGIN)
        // ====================================================================
        // Memeriksa apakah pengguna sebelumnya sudah pernah login dan sesinya masih tersimpan.
        if (userPreference.isLoggedIn()) {
            // Jika sudah login, langsung arahkan ke Dashboard sesuai role-nya (admin / user)
            // dan panggil 'return@onCreate' / 'return' agar halaman login ditutup dan tidak perlu dirender ulang.
            navigateToDashboard(userPreference.getRole() ?: "user")
            finish()
            return
        }

        // Memanggil fungsi modular untuk mengatur listener tombol dan observer LiveData
        setupListeners()
        observeLoginResult()
    }

    // ====================================================================
    // 3. EVENT HANDLING (AKSI TOMBOL & INTERAKSI PENGGUNA)
    // ====================================================================

    /**
     * Fungsi untuk mengatur aksi klik (Event Listener) pada tombol login dan teks tautan registrasi.
     */
    private fun setupListeners() {
        // Aksi ketika tombol LOGIN (btn_login) diklik
        binding.btnLogin.setOnClickListener {
            // Mengambil teks input username dan password dari EditText serta menghapus spasi berlebih dengan .trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validasi sederhana: pastikan kolom tidak kosong sebelum dikirim ke server
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Panggil fungsi login di AuthViewModel
                viewModel.login(username, password)
            } else {
                showToast("Username dan password tidak boleh kosong")
            }
        }

        // Aksi ketika teks "Belum punya akun? Daftar" diklik: Berpindah ke RegisterActivity
        binding.tvRegisterAction.setOnClickListener {
            navigateToActivity<RegisterActivity>()
        }
    }

    // ====================================================================
    // 4. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil proses login pada LiveData di AuthViewModel.
     */
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                // 1. KONDISI LOADING: Saat permintaan login sedang dikirim ke server
                is ResultState.Loading -> {
                    showLoading(true) // Tampilkan ProgressBar dan kunci form input
                }

                // 2. KONDISI SUKSES: Server mengembalikan respons sukses (HTTP 200) beserta token & data user
                is ResultState.Success -> {
                    showLoading(false)

                    // Mengambil data pengguna dari respons server
                    val responseData = result.data.data
                    val token = responseData?.token
                    val username = responseData?.username
                    val role = responseData?.role ?: "user" // Default role "user" jika kosong

                    // Validasi apakah token dan username yang diterima dari server benar-benar valid (tidak null/kosong)
                    if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
                        // PENTING: Simpan sesi login ke SharedPreferences lokal agar perangkat mengingat pengguna
                        userPreference.saveSession(token, username, role)

                        // Tampilkan pesan sukses dari server (jika ada) menggunakan Toast
                        result.data.message?.let { showToast(it) }

                        // Arahkan ke halaman Dashboard sesuai role (Admin / User) dan tutup LoginActivity (finishCurrent = true otomatis di helper)
                        navigateToDashboard(role, finishCurrent = true)
                    } else {
                        showToast("Token atau data user tidak valid")
                    }
                }

                // 3. KONDISI ERROR: Gagal login (misal: password salah, tidak ada internet, atau server error)
                is ResultState.Error -> {
                    showLoading(false)
                    // Tampilkan pesan kesalahan dari server kepada pengguna melalui Toast
                    showToast(result.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar dan mengunci/membuka tombol
     * serta input form secara otomatis menggunakan extension helper (`setLoading`).
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