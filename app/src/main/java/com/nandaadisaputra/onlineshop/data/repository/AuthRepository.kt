package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * AuthRepository adalah kelas Repository yang bertindak sebagai jembatan (Single Source of Truth)
 * antara AuthViewModel dengan sumber data jarak jauh (Remote API Service) untuk pengelolaan
 * autentikasi pengguna (Register, Login, dan Logout).
 *
 * @property apiService Objek interface Retrofit untuk memanggil endpoint API autentikasi.
 * Menggunakan nilai default [ApiConfig.provideApiService] karena endpoint Auth (Login/Register)
 * menggunakan layanan publik yang tidak memerlukan token di awal.
 */
class AuthRepository(
    private val apiService: ApiService = ApiConfig.provideApiService
) {

    /**
     * Fungsi untuk mendaftarkan akun pengguna baru ke server.
     * Mengembalikan data dalam bentuk aliran asinkron Kotlin Coroutines [Flow] yang memancarkan (emit)
     * status [ResultState] secara berurutan (Loading -> Success atau Error).
     *
     * @param username Nama pengguna
     * @param email Alamat email pengguna
     * @param alamat Alamat tempat tinggal
     * @param noWhatsApp Nomor telepon / WhatsApp
     * @param password Kata sandi akun
     * @return Flow<ResultState<AuthResponse<UserData>>> Aliran status hasil pendaftaran
     */
    fun register(
        username: String,
        email: String,
        alamat: String,
        noWhatsApp: String,
        password: String
    ): Flow<ResultState<AuthResponse<UserData>>> = flow {
        // 1. Pancarkan (emit) status Loading saat proses request dikirim ke server.
        // Ini akan memicu UI (RegisterActivity) untuk menampilkan ProgressBar dan menonaktifkan form.
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend registerUser() dari ApiService secara asinkron
            val response = apiService.registerUser(username, email, alamat, noWhatsApp, password)

            // 3. Memeriksa apakah server mengembalikan status sukses (status == true atau kode HTTP 200/201)
            if (response.status || response.code == 200 || response.code == 201) {
                emit(ResultState.Success(response))
            } else {
                emit(ResultState.Error(response.message ?: "Registrasi gagal."))
            }

        } catch (e: HttpException) {
            // 4. Menangkap error HTTP dari server (misal: email atau username sudah terdaftar)
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                "Terjadi kesalahan pada server (HTTP ${e.code()})"
            }
            emit(ResultState.Error(errorMessage))

        } catch (e: IOException) {
            // 5. Menangani gangguan koneksi jaringan fisik perangkat
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa sambungan Anda!"))

        } catch (e: Exception) {
            // 6. Menangani error tak terduga lainnya
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * Fungsi untuk melakukan proses masuk akun (Login) ke server.
     * Mengembalikan data dalam bentuk [Flow] yang memancarkan [ResultState].
     *
     * @param username Username akun
     * @param password Kata sandi akun
     * @return Flow<ResultState<AuthResponse<UserData>>> Aliran status hasil login
     */
    fun login(
        username: String,
        password: String
    ): Flow<ResultState<AuthResponse<UserData>>> = flow {
        // 1. Pancarkan status Loading saat proses login dimulai
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend loginUser() dari ApiService
            val response = apiService.loginUser(username, password)

            // 3. Periksa keberhasilan respons login dari server
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                emit(ResultState.Error(response.message ?: "Username atau password salah."))
            }

        } catch (e: HttpException) {
            // 4. Ekstraksi pesan error spesifik dari JSON server (misal: password salah)
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                "Terjadi kesalahan pada server (HTTP ${e.code()})"
            }
            emit(ResultState.Error(errorMessage))

        } catch (e: IOException) {
            // 5. Penanganan error koneksi internet
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa sambungan Anda!"))

        } catch (e: Exception) {
            // 6. Penanganan error sistem umum
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * Fungsi Repository untuk Logout / Keluar akun ke server agar token sesi dihapus.
     * Mengembalikan data dalam bentuk [Flow] yang memancarkan [ResultState].
     *
     * @param token Token autentikasi pengguna yang aktif
     * @return Flow<ResultState<AuthResponse<Any?>>> Aliran status hasil logout
     */
    fun logout(token: String): Flow<ResultState<AuthResponse<Any?>>> = flow {
        // 1. Pancarkan status Loading saat proses logout dimulai
        emit(ResultState.Loading)

        try {
            // 2. Memformat token agar sesuai dengan standar protokol HTTP Header ("Bearer <token>").
            // Pengecekan startsWith mencegah penulisan kata "Bearer " ganda.
            val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

            // 3. Memanggil fungsi suspend logoutUser() dari ApiService
            val response = apiService.logoutUser(bearerToken)

            // 4. Periksa keberhasilan respons logout
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                emit(ResultState.Error(response.message ?: "Gagal melakukan logout."))
            }

        } catch (e: HttpException) {
            emit(ResultState.Error("Kesalahan server (HTTP ${e.code()})"))
        } catch (e: IOException) {
            emit(ResultState.Error("Koneksi internet bermasalah."))
        } catch (e: Exception) {
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }
}