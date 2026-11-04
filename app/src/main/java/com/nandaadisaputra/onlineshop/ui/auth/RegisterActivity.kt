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

/**
 * RegisterActivity adalah halaman (View dalam arsitektur MVVM) yang bertugas menangani proses
 * pendaftaran akun baru (Register) bagi pengguna ke server, validasi form masukan,
 * serta navigasi kembali ke halaman Login.
 */
class RegisterActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_register.xml secara aman
    private lateinit var binding: ActivityRegisterBinding

    // Inisialisasi AuthViewModel menggunakan delegasi 'by viewModels()' untuk memproses logika registrasi ke server
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi modular untuk mengatur listener tombol dan observer LiveData
        setupListeners()
        observeRegisterResult()
    }

    // ====================================================================
    // 2. EVENT HANDLING (AKSI TOMBOL & INTERAKSI PENGGUNA)
    // ====================================================================

    /**
     * Fungsi untuk mengatur aksi klik (Event Listener) pada tombol register dan teks tautan login.
     */
    private fun setupListeners() {
        // Aksi ketika tombol REGISTER (btn_register) diklik oleh pengguna
        binding.btnRegister.setOnClickListener {
            // Mengambil teks input dari masing-masing form dan membersihkan spasi berlebih dengan .trim()
            val username = binding.etRegisterUsername.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val alamat = binding.etRegisterAddress.text.toString().trim()
            val noWhatsApp = binding.etRegisterWhatsapp.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            // Validasi form: pastikan seluruh kolom tidak boleh kosong sebelum dikirim ke server
            if (username.isNotEmpty() && email.isNotEmpty() && alamat.isNotEmpty() && noWhatsApp.isNotEmpty() && password.isNotEmpty()) {
                // Panggil fungsi register di AuthViewModel dengan mengirimkan seluruh data masukan
                viewModel.register(username, email, alamat, noWhatsApp, password)
            } else {
                // Tampilkan peringatan menggunakan Toast jika ada kolom yang masih kosong
                showToast("Semua kolom harus diisi!")
            }
        }

        // Aksi ketika teks "Sudah punya akun? Login disini" diklik: Kembali ke halaman Login
        binding.tvLoginAction.setOnClickListener {
            // Memanggil extension helper untuk berpindah ke LoginActivity
            navigateToLogin()
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil proses registrasi pada LiveData di AuthViewModel.
     */
    private fun observeRegisterResult() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                // 1. KONDISI LOADING: Saat proses pendaftaran sedang dikirim ke server
                is ResultState.Loading -> {
                    showLoading(true) // Tampilkan ProgressBar dan kunci form input
                }

                // 2. KONDISI SUKSES: Server berhasil menyimpan data akun baru (HTTP 200 / 201)
                is ResultState.Success -> {
                    showLoading(false)
                    // Tampilkan pesan sukses dari server (jika ada) atau pesan default
                    showToast(result.data.message ?: "Registrasi berhasil")

                    // Mengarahkan kembali pengguna ke halaman LoginActivity agar bisa langsung masuk akun
                    navigateToLogin()
                }

                // 3. KONDISI ERROR: Pendaftaran gagal (misal: email/username sudah terpakai atau tidak ada internet)
                is ResultState.Error -> {
                    showLoading(false)
                    // Tampilkan pesan kesalahan dari server kepada pengguna melalui Toast
                    showToast(result.message)
                }
            }
        }
    }

    // ====================================================================
    // 4. FUNGSI BANTUAN (HELPER METHODS)
    // ====================================================================

    /**
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan tombol
     * serta kolom form input secara otomatis menggunakan extension helper (`setLoadingRegister`).
     */
    private fun showLoading(isLoading: Boolean) {
        // Memanggil extension function dengan menyertakan tombol register dan elemen-elemen input form
        binding.progressBar.setLoadingRegister(
            isLoading,
            binding.btnRegister,
            binding.etRegisterEmail,
            binding.etRegisterAddress,
            binding.etRegisterUsername,
            binding.etRegisterWhatsapp,
            binding.tilRegisterPassword
        )
    }
}