package com.nandaadisaputra.onlineshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel adalah kelas pengelola state UI dan logika bisnis (ViewModel dalam arsitektur MVVM)
 * yang digunakan bersama oleh ProfileFragment (untuk fitur Logout) dan EditProfileActivity (untuk fitur Update Profile)[cite: 1, 2].
 *
 * @property repository Instance dari ProfileRepository yang disuntikkan melalui konstruktor (Constructor Injection)
 * menggunakan ProfileViewModelFactory.
 */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    // =========================================================================================
    // 1. STATE UNTUK UPDATE PROFILE (Digunakan di EditProfileActivity)[cite: 2]
    // =========================================================================================

    // Backing Property (_updateState): Bersifat 'private' dan 'MutableStateFlow' agar nilainya
    // HANYA bisa diubah dari dalam ProfileViewModel ini saja (Penerapan prinsip Encapsulation).
    // Nilai awal diisi 'null' (kondisi diam/idle sebelum tombol Simpan diklik).
    private val _updateState = MutableStateFlow<ResultState<AuthResponse<UserData>>?>(null)

    // Exposed State (updateState): Bersifat 'public' dan bertipe 'StateFlow' (Read-Only / hanya bisa dibaca).
    // Variabel inilah yang diamati (di-collect) oleh EditProfileActivity untuk mengubah tampilan UI
    // (menampilkan loading, pesan sukses, atau pesan error)[cite: 6].
    val updateState: StateFlow<ResultState<AuthResponse<UserData>>?> = _updateState.asStateFlow()


    // =========================================================================================
    // 2. STATE UNTUK LOGOUT (Digunakan di ProfileFragment)[cite: 1]
    // =========================================================================================

    // Backing Property (_logoutState): Menyimpan state proses logout secara internal di dalam ViewModel.
    private val _logoutState = MutableStateFlow<ResultState<AuthResponse<Any?>>?>(null)

    // Exposed State (logoutState): State publik bersifat Read-Only yang diamati oleh ProfileFragment
    // untuk mengetahui kapan proses logout ke server selesai dijalankan.
    val logoutState: StateFlow<ResultState<AuthResponse<Any?>>?> = _logoutState.asStateFlow()


    // =========================================================================================
    // 3. FUNGSI AKSI (EVENT HANDLER DARI UI KE REPOSITORY)
    // =========================================================================================

    /**
     * Fungsi untuk memicu proses pembaruan data profil (username & password) ke server[cite: 3].
     *
     * @param username Username baru yang diinputkan pengguna[cite: 3]
     * @param oldPass Password lama pengguna untuk verifikasi keamanan di server[cite: 3]
     * @param newPass Password baru yang ingin disimpan[cite: 3]
     */
    fun updateProfile(username: String, oldPass: String, newPass: String) {
        // viewModelScope.launch menjalankan proses jaringan secara asinkron (di background thread)
        // yang terikat otomatis dengan siklus hidup (Lifecycle) ViewModel.
        // Jika pengguna keluar dari halaman dan ViewModel dihancurkan, coroutine ini otomatis dibatalkan
        // sehingga mencegah kebocoran memori (Memory Leak).
        viewModelScope.launch {
            // Memanggil fungsi updateProfile dari Repository yang mengembalikan aliran data (Flow),
            // lalu '.collect' akan menangkap setiap perubahan status (Loading -> Success / Error)[cite: 6]
            // dan menyimpannya ke dalam _updateState.
            repository.updateProfile(username, oldPass, newPass).collect { state ->
                _updateState.value = state
            }
        }
    }

    /**
     * Fungsi untuk memicu proses keluar akun (Logout) ke server agar token sesi dihapus[cite: 5].
     *
     * @param token Bearer Token pengguna yang sedang login (diambil dari SharedPreferences)[cite: 7, 10]
     */
    fun logout(token: String) {
        // Menjalankan coroutine di dalam scope ViewModel
        viewModelScope.launch {
            // Memanggil fungsi logoutUser dari Repository dan menangkap setiap pancaran statusnya
            // (Loading -> Success / Error) untuk diteruskan ke UI melalui _logoutState[cite: 6].
            repository.logoutUser(token).collect { state ->
                _logoutState.value = state
            }
        }
    }
}