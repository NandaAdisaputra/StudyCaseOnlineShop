package com.nandaadisaputra.onlineshop.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // ====================================================================
    // 1. LIVEDATA UNTUK MENAMPUNG STATE (REGISTER, LOGIN, LOGOUT)
    // ====================================================================
    private val _registerResult = MutableLiveData<ResultState<AuthResponse<UserData>>>()
    val registerResult: LiveData<ResultState<AuthResponse<UserData>>> get() = _registerResult

    private val _loginResult = MutableLiveData<ResultState<AuthResponse<UserData>>>()
    val loginResult: LiveData<ResultState<AuthResponse<UserData>>> get() = _loginResult

    private val _logoutResult = MutableLiveData<ResultState<AuthResponse<Any?>>>()
    val logoutResult: LiveData<ResultState<AuthResponse<Any?>>> get() = _logoutResult

    // ====================================================================
    // 2. FUNGSI UTAMA (BUSINESS LOGIC)
    // ====================================================================

    fun register(username: String, email: String, alamat: String, noWhatsApp: String, password: String) {
        _registerResult.executeApiCall {
            repository.register(username, email, alamat, noWhatsApp, password)
        }
    }

    fun login(username: String, password: String) {
        _loginResult.executeApiCall {
            repository.login(username, password)
        }
    }

    fun logout(token: String) {
        _logoutResult.executeApiCall {
            repository.logout(token)
        }
    }

    // ====================================================================
    // 3. HELPER FUNCTION (UNTUK MERINGKAS TRY-CATCH & LOADING STATE)
    // ====================================================================

    /**
     * Helper extension function khusus MutableLiveData untuk menangani
     * pemanggilan API secara aman dengan type-safety yang bersih tanpa casting manual.
     */
    private fun <T> MutableLiveData<ResultState<AuthResponse<T>>>.executeApiCall(
        apiCall: suspend () -> AuthResponse<T>
    ) {
        // 1. Ubah state menjadi Loading saat permintaan dimulai
        value = ResultState.Loading

        viewModelScope.launch {
            try {
                // 2. Panggil fungsi suspend dari repository
                val response = apiCall()

                if (response.status) {
                    // 3. Jika berhasil, kirim data sukses secara aman (Type-safe)
                    value = ResultState.Success(response)
                } else {
                    // 4. Jika gagal dari sisi server (status false), kirim pesan error
                    value = ResultState.Error(response.message ?: "Terjadi kesalahan pada server")
                }
            } catch (e: Exception) {
                // 5. Jika terjadi gangguan jaringan atau exception, tangkap di sini
                value = ResultState.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
            }
        }
    }
}