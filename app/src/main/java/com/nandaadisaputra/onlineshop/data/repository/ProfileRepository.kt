package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * ProfileRepository adalah kelas Repository yang bertindak sebagai jembatan (Single Source of Truth)
 * antara ProfileViewModel dengan sumber data jarak jauh (Remote API Service)[cite: 9].
 *
 * Kelas ini bertanggung jawab penuh untuk menangani transaksi jaringan yang berkaitan dengan
 * akun pengguna setelah login, yaitu memperbarui profil (Update Profile) dan keluar akun (Logout)[cite: 3, 5].
 *
 * @property apiService Objek interface Retrofit untuk memanggil endpoint API di server[cite: 5, 9].
 */
class ProfileRepository(private val apiService: ApiService) {

    /**
     * Fungsi untuk memperbarui profil pengguna (username & password) ke server[cite: 3].
     * Mengembalikan aliran data asinkron Kotlin Coroutines [Flow] yang memancarkan (emit)
     * status [ResultState] secara berurutan (Loading -> Success atau Error)[cite: 6, 9].
     *
     * @param username Username baru yang ingin disimpan[cite: 3].
     * @param oldPassword Password lama untuk verifikasi keamanan di sisi server[cite: 3].
     * @param newPassword Password baru pengganti password lama[cite: 3].
     * @return Flow<ResultState<AuthResponse<UserData>>> Aliran status hasil pembaruan profil[cite: 3, 5, 6].
     */
    fun updateProfile(
        username: String,
        oldPassword: String,
        newPassword: String
    ): Flow<ResultState<AuthResponse<UserData>>> = flow {
        // 1. Pancarkan (emit) status Loading terlebih dahulu saat request ke server dimulai.
        // Ini akan memicu UI (EditProfileActivity) untuk menampilkan ProgressBar dan menonaktifkan tombol[cite: 2, 6, 8, 9].
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend updateProfile() dari ApiService secara asinkron[cite: 9].
            // Catatan: Header "Authorization: Bearer <token>" sudah otomatis disisipkan oleh AuthInterceptor[cite: 4, 7].
            val response = apiService.updateProfile(username, oldPassword, newPassword)

            // 3. Memeriksa apakah respons dari server bernilai sukses (status == true atau kode HTTP 200)[cite: 3, 9]
            if (response.status || response.code == 200) {
                // Jika berhasil, pancarkan status Success beserta data pengguna terbaru (response) ke ViewModel[cite: 3, 6, 9]
                emit(ResultState.Success(response))
            } else {
                // Jika gagal (tetapi HTTP status tetap 200), ambil pesan error dari response server.
                // Elvis operator (?:) digunakan untuk memberikan pesan default jika response.message bernilai null[cite: 6, 9].
                emit(ResultState.Error(response.message ?: "Gagal memperbarui profil."))
            }

        } catch (e: HttpException) {
            // 4. Menangani kesalahan protokol HTTP dari server (misal: HTTP 400 Bad Request atau 401 Unauthorized
            // ketika pengguna memasukkan password lama yang salah)[cite: 9].
            // Ketika HTTP status code bukan 2xx, Retrofit otomatis melempar HttpException dan menyimpan
            // pesan JSON asli dari server di dalam 'errorBody()'.
            val errorBody = e.response()?.errorBody()?.string()

            // Mengekstrak nilai properti "message" dari string JSON errorBody menggunakan JSONObject
            // agar pengguna mendapatkan pesan yang jelas (contoh: "Password lama tidak sesuai!")[cite: 3].
            val errorMessage = try {
                JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                // Fallback (pesan cadangan) jika response error dari server bukan berformat JSON
                "Terjadi kesalahan pada server (HTTP ${e.code()})"
            }
            emit(ResultState.Error(errorMessage))

        } catch (e: IOException) {
            // 5. Menangani kesalahan koneksi jaringan fisik pada perangkat pengguna
            // (Contoh: Tidak ada kuota internet, Wi-Fi terputus, atau koneksi Time Out)[cite: 9]
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa sambungan Anda!"))

        } catch (e: Exception) {
            // 6. Menangani kesalahan tak terduga lainnya (misal: Kegagalan parsing JSON ke Data Class)[cite: 9]
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * Fungsi untuk melakukan proses keluar akun (Logout) ke server agar token sesi dihapus/dihanguskan[cite: 5].
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState][cite: 6, 9].
     *
     * @param token Token autentikasi pengguna yang sedang aktif dari SharedPreferences[cite: 7, 10].
     * @return Flow<ResultState<AuthResponse<Any?>>> Aliran status hasil proses logout[cite: 5, 6].
     */
    fun logoutUser(token: String): Flow<ResultState<AuthResponse<Any?>>> = flow {
        // 1. Pancarkan status Loading saat proses logout dimulai[cite: 6, 9]
        emit(ResultState.Loading)

        try {
            // 2. Memformat token agar sesuai dengan standar protokol HTTP Authorization Header ("Bearer <token>")[cite: 5, 7].
            // Pengecekan startsWith("Bearer ") mencegah penulisan kata "Bearer " ganda.
            val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

            // 3. Memanggil fungsi suspend logoutUser() dari ApiService dengan mengirimkan header token[cite: 5, 9]
            val response = apiService.logoutUser(bearerToken)

            // 4. Periksa apakah server berhasil memproses permintaan logout[cite: 9]
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                emit(ResultState.Error(response.message ?: "Gagal melakukan logout."))
            }

        } catch (e: HttpException) {
            // 5. Menangani kesalahan HTTP dari sisi server (misal: Error 401 jika token sudah kadaluarsa, atau 500)[cite: 9]
            emit(ResultState.Error("Kesalahan server (HTTP ${e.code()})"))

        } catch (e: IOException) {
            // 6. Menangani kesalahan koneksi internet[cite: 9]
            emit(ResultState.Error("Koneksi internet bermasalah."))

        } catch (e: Exception) {
            // 7. Menangani kesalahan umum/sistem lainnya[cite: 9]
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }
}