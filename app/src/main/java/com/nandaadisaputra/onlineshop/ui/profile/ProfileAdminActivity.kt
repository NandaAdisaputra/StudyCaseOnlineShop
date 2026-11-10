package com.nandaadisaputra.onlineshop.ui.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.navigateToLoginOnLogout
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.ActivityProfileAdminBinding
import com.nandaadisaputra.onlineshop.databinding.DialogConfirmLogoutBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AuthViewModel

/**
 * ProfileAdminActivity adalah halaman (View dalam arsitektur MVVM) khusus untuk akun berhak akses Administrator.
 * Halaman ini bertugas menampilkan informasi identitas profil admin yang sedang login serta
 * menyediakan tombol interaktif beserta dialog konfirmasi untuk keluar akun (Logout) melalui server dan sesi lokal.
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

        // Memanggil fungsi-fungsi modular untuk struktur kode yang rapi
        setupView()
        setupListeners()
        observeLogoutResult()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN, DIALOG KONFIRMASI & EVENT HANDLING
    // ====================================================================

    /**
     * Fungsi untuk menyiapkan data awal tampilan profil admin di layar.
     */
    private fun setupView() {
        // Mengambil username admin yang tersimpan di SharedPreferences lokal
        val username = userPreference.getUsername()
        // Menampilkan username ke TextView (tvUsername). Jika null/kosong, tampilkan teks default dari strings.xml.
        binding.tvUsername.text = if (!username.isNullOrEmpty()) {
            username
        } else {
            getString(R.string.admin_username)
        }
    }

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada komponen UI.
     */
    private fun setupListeners() {
        // Aksi ketika tombol kembali (<) diklik oleh admin
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Aksi ketika tombol LOGOUT (btn_logout) diklik -> Tampilkan Alert Dialog Konfirmasi terlebih dahulu
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Fungsi untuk menampilkan Custom AlertDialog konfirmasi keluar aplikasi sesuai desain UI/UX.
     */
    private fun showLogoutConfirmationDialog() {
        val dialogBinding = DialogConfirmLogoutBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Membuat latar belakang window dialog transparan agar sudut melengkung CardView terlihat rapi
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Jika tombol BATAL diklik, tutup dialog
        dialogBinding.btnBatal.setOnClickListener {
            alertDialog.dismiss()
        }

        // Jika tombol YA diklik, tutup dialog dan jalankan proses logout
        dialogBinding.btnYa.setOnClickListener {
            alertDialog.dismiss()
            executeLogout()
        }

        alertDialog.show()
    }

    /**
     * Fungsi untuk mengeksekusi permintaan logout ke server atau sesi lokal.
     */
    private fun executeLogout() {
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
                    val message = result.data.message?.ifEmpty {
                        getString(R.string.msg_logout_success)
                    } ?: getString(R.string.msg_logout_success)
                    showToast(message)
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
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan tombol
     * secara otomatis menggunakan extension helper (`setLoading`).
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(isLoading, binding.btnLogout, binding.btnBack)
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