package com.nandaadisaputra.onlineshop.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.ActivityEditProfileBinding
import com.nandaadisaputra.onlineshop.ui.home.MainActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.ProfileViewModel
import com.nandaadisaputra.onlineshop.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

/**
 * EditProfileActivity adalah halaman (View dalam arsitektur MVVM) yang bertanggung jawab
 * untuk menampilkan formulir ubah profil (Username & Password) serta mengamati (observe)
 * status pembaruan data dari ProfileViewModel.
 */
class EditProfileActivity : AppCompatActivity() {

    // 1. Deklarasi ViewBinding untuk mengakses komponen XML (activity_edit_profile.xml)
    // dengan aman (Null-Safe & Type-Safe) tanpa perlu menggunakan findViewById()
    private lateinit var binding: ActivityEditProfileBinding

    // 2. Deklarasi UserPreference untuk mengelola data sesi pengguna yang tersimpan
    // di penyimpanan lokal perangkat (SharedPreferences)
    private lateinit var userPreference: UserPreference

    // 3. Inisialisasi ProfileViewModel menggunakan delegasi 'by viewModels'.
    // Kita menggunakan Factory (ProfileViewModelFactory) karena ProfileViewModel membutuhkan
    // parameter konstruktor berupa Repository dan Context (Dependency Injection manual).
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Menginisialisasi objek UserPreference dengan mengirimkan Context Activity ini
        userPreference = UserPreference(this)

        // Memanggil fungsi-fungsi modular agar struktur kode di onCreate tetap rapi dan mudah dibaca
        setupInitialData()          // Menampilkan data awal (username saat ini) ke dalam form
        setupListeners()            // Mengatur aksi klik pada tombol-tombol
        observeUpdateProfileState() // Mengamati perubahan status (Loading, Success, Error) dari ViewModel
    }

    /**
     * Fungsi untuk mengisi nilai awal pada form input sebelum diubah oleh pengguna.
     */
    private fun setupInitialData() {
        // Mengambil username yang sedang aktif dari SharedPreferences
        val currentUsername = userPreference.getUsername()

        // Menampilkan username tersebut ke dalam EditText et_username
        binding.etUsername.setText(currentUsername)
    }

    /**
     * Fungsi untuk menangani interaksi klik pengguna (Event Listener) pada komponen UI.
     */
    private fun setupListeners() {
        // Menggunakan scope function 'apply' agar tidak perlu menulis 'binding.' berulang kali
        binding.apply {

            // Aksi ketika ikon panah kembali (iv_back) diklik
            ivBack.setOnClickListener {
                // Kembali ke halaman sebelumnya (setara dengan menekan tombol Back di HP)
                onBackPressedDispatcher.onBackPressed()
            }

            // Aksi ketika tombol SIMPAN (btn_simpan) diklik
            btnSimpan.setOnClickListener {
                // Mengambil teks yang diketik pengguna dari EditText, lalu '.trim()' digunakan
                // untuk menghapus spasi kosong yang tidak sengaja terketik di awal atau akhir teks
                val username = etUsername.text.toString().trim()
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()

                // --- VALIDASI INPUT FORM SEBELUM DIKIRIM KE SERVER ---

                // 1. Validasi apakah kolom username kosong
                if (username.isEmpty()) {
                    etUsername.error = "Username tidak boleh kosong"
                    etUsername.requestFocus() // Mengarahkan kursor langsung ke kolom yang bermasalah
                    return@setOnClickListener // Menghentikan eksekusi kode di bawahnya
                }

                // 2. Validasi apakah kolom password lama kosong
                if (oldPassword.isEmpty()) {
                    etOldPassword.error = "Password lama wajib diisi"
                    etOldPassword.requestFocus()
                    return@setOnClickListener
                }

                // 3. Validasi apakah kolom password baru kosong
                if (newPassword.isEmpty()) {
                    etNewPassword.error = "Password baru wajib diisi"
                    etNewPassword.requestFocus()
                    return@setOnClickListener
                }

                // 4. Validasi panjang minimal password baru (minimal 6 karakter)
                if (newPassword.length < 6) {
                    etNewPassword.error = "Password baru minimal 6 karakter"
                    etNewPassword.requestFocus()
                    return@setOnClickListener
                }

                // Jika seluruh validasi di atas lolos, panggil fungsi updateProfile() di ViewModel
                // untuk mengirimkan data ke API server
                viewModel.updateProfile(username, oldPassword, newPassword)
            }
        }
    }

    /**
     * Fungsi untuk mengamati (observe) aliran data StateFlow dari ProfileViewModel.
     * Di sinilah pola reaktif MVVM bekerja: UI merespons setiap perubahan state secara otomatis.
     */
    private fun observeUpdateProfileState() {
        // Menjalankan coroutine yang terikat dengan siklus hidup (Lifecycle) Activity
        lifecycleScope.launch {
            // repeatOnLifecycle(Lifecycle.State.STARTED) memastikan pengumpulan data (collect)
            // hanya aktif saat Activity terlihat di layar (STARTED) dan otomatis berhenti sementara
            // saat aplikasi berada di latar belakang (hemat baterai & mencegah memory leak / crash).
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateState.collect { state ->
                    when (state) {
                        // 1. KONDISI LOADING: Saat request API sedang dikirim dan menunggu balasan server
                        is ResultState.Loading -> {
                            // Menampilkan ProgressBar sekaligus menonaktifkan (disable) tombol & input
                            // menggunakan Extension Function agar pengguna tidak melakukan spam klik
                            binding.progressBar.setLoading(
                                true,
                                binding.btnSimpan,
                                binding.etUsername,
                                binding.etOldPassword,
                                binding.etNewPassword
                            )
                        }

                        // 2. KONDISI SUKSES: Saat server mengembalikan respons berhasil (HTTP 200)
                        is ResultState.Success -> {
                            // Menyembunyikan ProgressBar dan mengaktifkan kembali komponen UI
                            binding.progressBar.setLoading(
                                false,
                                binding.btnSimpan,
                                binding.etUsername,
                                binding.etOldPassword,
                                binding.etNewPassword
                            )

                            // Mengambil objek response dari server
                            val response = state.data

                            // Mengambil username terbaru dari objek JSON ("data" -> "username").
                            // Jika null (menggunakan Elvis Operator ?:), ambil dari teks EditText.
                            val updatedUsername = response.data?.username
                                ?: binding.etUsername.text.toString().trim()

                            // PENTING: Perbarui juga username yang tersimpan di SharedPreferences lokal
                            // agar tampilan nama di halaman Profil / Home langsung ikut berubah
                            userPreference.updateUsername(updatedUsername)

                            // Menampilkan pesan berhasil dari server menggunakan Extension Function showToast
                            showToast(response.message ?: "Profil berhasil diperbarui!")

                            // Berpindah kembali ke MainActivity dan menutup EditProfileActivity (finishCurrent = true).
                            // Flag CLEAR_TOP & NEW_TASK digunakan untuk membersihkan tumpukan halaman (Activity Stack)
                            // agar MainActivity dimuat ulang dengan data profil yang baru.
                            navigateToActivity<MainActivity>(finishCurrent = true) {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        }

                        // 3. KONDISI ERROR: Saat terjadi kegagalan (password lama salah, internet mati, atau server error)
                        is ResultState.Error -> {
                            // Menyembunyikan ProgressBar dan mengaktifkan kembali tombol & form input
                            binding.progressBar.setLoading(
                                false,
                                binding.btnSimpan,
                                binding.etUsername,
                                binding.etOldPassword,
                                binding.etNewPassword
                            )

                            // Menampilkan pesan error kepada pengguna melalui Toast
                            showToast(state.message)
                        }

                        // 4. KONDISI AWAL (IDLE): Saat halaman baru dibuka dan belum ada aksi simpan
                        null -> Unit
                    }
                }
            }
        }
    }
}