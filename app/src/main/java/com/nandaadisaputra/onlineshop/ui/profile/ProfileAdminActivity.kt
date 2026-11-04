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

/**
 * ProfileAdminActivity adalah halaman (View dalam arsitektur MVVM) khusus untuk akun berhak akses Administrator.
 * Halaman ini bertugas menampilkan informasi identitas profil admin yang sedang login serta
 * menyediakan tombol interaktif untuk keluar akun (Logout) melalui server dan sesi lokal.
 */
class ProfileAdminActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_profile_admin.xml secara aman
    private lateinit var binding: ActivityProfileAdminBinding

    // Inisialisasi AuthViewModel menggunakan delegasi 'by viewModels()' untuk mengelola logika logout
    private val authViewModel: AuthViewModel by viewModels()

    // Deklarasi UserPreference untuk membaca dan menghapus data sesi SharedPreferences di perangkat
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityProfileAdminBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Menginisialisasi objek UserPreference dengan Context Activity ini
        userPreference = UserPreference(this)

        // Memanggil fungsi-fungsi modular untuk inisruktur kode yang rapi
        setupView()
        setupListeners()
        observeLogoutResult()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN & EVENT HANDLING
    // ====================================================================

    /**
     * Fungsi untuk menyiapkan data awal tampilan profil admin di layar.
     */
    private fun setupView() {
        // Mengambil username admin yang tersimpan di SharedPreferences lokal
        val username = userPreference.getUsername()
        // Menampilkan username ke TextView (tvUsername). Jika null, tampilkan teks default "Administrator".
        binding.tvUsername.text = username ?: "Administrator"
    }

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada komponen UI.
     */
    private fun setupListeners() {
        // Aksi ketika tombol LOGOUT (btn_logout) diklik oleh admin
        binding.btnLogout.setOnClickListener {
            // Mengambil token autentikasi aktif dari SharedPreferences
            val token = userPreference.getToken()

            if (!token.isNullOrEmpty()) {
                // Jika token ada, panggil fungsi logout di AuthViewModel untuk menghapus sesi di server
                authViewModel.logout(token)
            } else {
                // Jika token kosong, langsung bersihkan sesi lokal saja
                performLocalLogout()
            }
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil proses logout pada LiveData di AuthViewModel.
     */
    private fun observeLogoutResult() {
        authViewModel.logoutResult.observe(this) { result ->
            when (result) {
                // 1. KONDISI LOADING: Saat permintaan logout sedang dikirim ke server
                is ResultState.Loading -> {
                    showLoading(true) // Tampilkan ProgressBar dan nonaktifkan tombol logout
                }

                // 2. KONDISI SUKSES: Server berhasil memproses penghapusan sesi token
                is ResultState.Success -> {
                    showLoading(false)
                    showToast(result.data.message ?: "Logout berhasil")
                    performLocalLogout() // Hapus sesi lokal dan kembali ke halaman Login
                }

                // 3. KONDISI ERROR: Gagal terhubung ke server atau token sudah tidak valid (misal: 401 Unauthorized)
                is ResultState.Error -> {
                    showLoading(false)
                    showToast(result.message)
                    // PENTING: Tetap lakukan logout lokal meskipun server mengembalikan error,
                    // demi menjaga keamanan perangkat agar aplikasi tidak nyangkut di halaman admin.
                    performLocalLogout()
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan tombol Logout
     * secara otomatis menggunakan extension helper (`setLoading`).
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(isLoading, binding.btnLogout)
    }

    /**
     * Fungsi pembantu untuk membersihkan seluruh sesi lokal di perangkat dan mengarahkan
     * kembali ke halaman Login secara bersih menggunakan extension helper (`navigateToLoginOnLogout`).
     */
    private fun performLocalLogout() {
        // Menghapus seluruh data sesi di SharedPreferences (token, username, role, dll.)
        userPreference.logout()

        // Pindah kembali ke halaman LoginActivity dan menghapus seluruh tumpukan halaman sebelumnya (Activity Stack)
        navigateToLoginOnLogout()
    }
}