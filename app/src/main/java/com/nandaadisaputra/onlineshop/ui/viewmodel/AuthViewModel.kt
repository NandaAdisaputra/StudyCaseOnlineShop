package com.nandaadisaputra.onlineshop.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * AuthViewModel adalah kelas pengelola logika bisnis dan state (ViewModel dalam arsitektur MVVM)
 * yang khusus menangani proses autentikasi pengguna, meliputi Registrasi Akun (Register),
 * Masuk Akun (Login), dan Keluar Akun (Logout).
 *
 * @property repository Objek AuthRepository yang bertindak sebagai sumber data (Data Source).
 * Nilai default menggunakan instansiasi `AuthRepository()` secara langsung.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // ====================================================================
    // 1. LIVEDATA UNTUK MENAMPUNG STATE (REGISTER, LOGIN, LOGOUT)
    // Penerapan Prinsip Encapsulation (Backing Property Pattern):
    // Variabel bergaris bawah (_) bersifat private & mutable (bisa diubah di dalam ViewModel).
    // Variabel tanpa garis bawah bersifat public & read-only (hanya bisa diamati oleh UI/Activity).
    // ====================================================================

    // LiveData State untuk proses Register
    private val _registerResult = MutableLiveData<ResultState<AuthResponse<UserData>>>()
    val registerResult: LiveData<ResultState<AuthResponse<UserData>>> get() = _registerResult

    // LiveData State untuk proses Login
    private val _loginResult = MutableLiveData<ResultState<AuthResponse<UserData>>>()
    val loginResult: LiveData<ResultState<AuthResponse<UserData>>> get() = _loginResult

    // LiveData State untuk proses Logout
    private val _logoutResult = MutableLiveData<ResultState<AuthResponse<Any?>>>()
    val logoutResult: LiveData<ResultState<AuthResponse<Any?>>> get() = _logoutResult


    // ====================================================================
    // 2. FUNGSI UTAMA (BUSINESS LOGIC / EVENT HANDLER)
    // Fungsi-fungsi ini dipanggil oleh UI (seperti LoginActivity atau RegisterActivity)
    // saat pengguna menekan tombol aksi.
    // ====================================================================

    /**
     * Fungsi untuk memicu proses pendaftaran akun baru ke server.
     */
    fun register(
        username: String,
        email: String,
        alamat: String,
        noWhatsApp: String,
        password: String
    ) {
        // Memanggil fungsi register di repository yang mengembalikan Flow,
        // lalu menangkap setiap perubahan statusnya menggunakan helper 'collectFlow'.
        _registerResult.collectFlow(
            repository.register(username, email, alamat, noWhatsApp, password)
        )
    }

    /**
     * Fungsi untuk memicu proses masuk akun (Login) ke server.
     */
    fun login(username: String, password: String) {
        _loginResult.collectFlow(
            repository.login(username, password)
        )
    }

    /**
     * Fungsi untuk memicu proses keluar akun (Logout) ke server.
     */
    fun logout(token: String) {
        _logoutResult.collectFlow(
            repository.logout(token)
        )
    }


    // ====================================================================
    // 3. HELPER FUNCTION (UNTUK MENANGKAP ALIRAN FLOW DARI REPOSITORY)
    // Konsep DRY (Don't Repeat Yourself): Menghindari penulisan blok viewModelScope
    // dan collectLatest yang berulang di setiap fungsi autentikasi.
    // ====================================================================

    /**
     * Helper extension function khusus [MutableLiveData] untuk mengumpulkan (collect)
     * data aliran asinkron [Flow] yang dipancarkan oleh Repository secara aman.
     *
     * @param T Tipe data generik dari respons server
     * @Flow Flow berisi [ResultState] (Loading -> Success / Error) dari repository
     */
    private fun <T> MutableLiveData<ResultState<T>>.collectFlow(
        flow: Flow<ResultState<T>>
    ) {
        // Menjalankan coroutine di dalam viewModelScope.
        //viewModelScope terikat otomatis dengan siklus hidup ViewModel, sehingga jika ViewModel
        // dihancurkan, proses jaringan yang sedang berjalan akan otomatis dibatalkan (mencegah memory leak).
        viewModelScope.launch {
            // collectLatest mendengarkan pancaran data Flow dan membatalkan pekerjaan sebelumnya
            // jika ada pancaran status baru yang masuk.
            flow.collectLatest { result ->
                // Memperbarui nilai LiveData agar UI (Activity) merespons secara reaktif
                value = result
            }
        }
    }
}