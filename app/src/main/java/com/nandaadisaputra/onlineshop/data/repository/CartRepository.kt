package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.CartListResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

/**
 * CartRepository adalah kelas Repository yang bertindak sebagai jembatan (Single Source of Truth)
 * antara CartViewModel dengan sumber data jarak jauh (Remote API Service) untuk pengelolaan
 * daftar keranjang belanja / riwayat pesanan pengguna[cite: 9, 15].
 *
 * Dengan adanya Repository, ViewModel tidak perlu tahu bagaimana cara data diambil (apakah dari
 * internet via Retrofit atau dari database lokal), sehingga kode menjadi lebih bersih dan modular[cite: 9].
 *
 * @property apiService Objek interface Retrofit untuk memanggil endpoint API di server[cite: 5, 9].
 */
class CartRepository(private val apiService: ApiService) {

    /**
     * Fungsi untuk mengambil daftar pesanan di keranjang belanja pengguna dari server[cite: 15].
     * Mengembalikan data dalam bentuk aliran asinkron Kotlin Coroutines [Flow] yang memancarkan (emit)
     * status [ResultState] secara berurutan (Loading -> Success atau Error)[cite: 6, 9].
     *
     * @return Flow<ResultState<CartListResponse>> Aliran status hasil pemanggilan API[cite: 6, 9].
     */
    fun getCartList(): Flow<ResultState<CartListResponse>> = flow {
        // 1. Pancarkan (emit) status Loading terlebih dahulu saat proses request ke server dimulai.
        // Hal ini akan memicu UI (CartFragment) untuk menampilkan indikator ProgressBar[cite: 6, 9].
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend getCartList() dari ApiService untuk meminta data ke server[cite: 9, 15].
            // Proses ini berjalan secara asinkron tanpa membuat layar aplikasi macet (freeze).
            val response = apiService.getCartList()

            // 3. Memeriksa apakah respons dari server bernilai sukses (status == true atau kode HTTP 200)[cite: 9, 15]
            if (response.status || response.code == 200) {
                // Jika berhasil, pancarkan status Success beserta seluruh data keranjang (response) ke ViewModel[cite: 6, 9]
                emit(ResultState.Success(response))
            } else {
                // Jika server mengembalikan status gagal, pancarkan status Error dengan pesan dari server.
                // Elvis operator (?:) digunakan untuk memberikan pesan cadangan jika response.message bernilai null[cite: 6, 9].
                emit(
                    ResultState.Error(
                        response.message ?: "Gagal memuat daftar keranjang belanja."
                    )
                )
            }

        } catch (e: HttpException) {
            // 4. Menangani kesalahan protokol HTTP dari sisi server
            // (Contoh: Error 401 Unauthorized, 404 Not Found, atau 500 Internal Server Error)[cite: 9, 14]
            emit(ResultState.Error("Kesalahan server (HTTP ${e.code()})"))

        } catch (e: IOException) {
            // 5. Menangani kesalahan jaringan fisik pada perangkat pengguna
            // (Contoh: Wi-Fi/Data seluler mati, tidak ada kuota internet, atau koneksi Request Time Out)[cite: 9]
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa sambungan Anda!"))

        } catch (e: Exception) {
            // 6. Menangani kesalahan umum atau tak terduga lainnya
            // (Contoh: Kegagalan konversi/parsing struktur JSON ke Data Class Kotlin)[cite: 9]
            emit(ResultState.Error("Terjadi kesalahan: ${e.localizedMessage}"))
        }
    }
}