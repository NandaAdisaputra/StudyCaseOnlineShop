package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

/**
 * ProductRepository adalah kelas Repository yang bertindak sebagai jembatan (Single Source of Truth)
 * antara ViewModel dengan sumber data jarak jauh (Remote API Service) untuk pengelolaan produk,
 * meliputi pengambilan daftar produk (pagination) dan pencarian produk (search).
 */
class ProductRepository(private val apiService: ApiService) {

    /**
     * Fungsi untuk mengambil daftar produk dari server dengan sistem halaman (Pagination).
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan (emit) [ResultState].
     *
     * @param page Halaman data produk yang ingin diminta
     * @param limit Batas maksimal jumlah item produk per halaman
     * @return Flow<ResultState<ProductResponse>> (Status Loading, Success, atau Error)
     */
    fun getProducts(page: Int, limit: Int): Flow<ResultState<ProductResponse>> = flow {
        // 1. Pancarkan status Loading saat permintaan data ke server mulai dijalankan
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend dari ApiService untuk mengambil produk berdasarkan halaman
            val response = apiService.getProducts(page, limit)

            // 3. Memeriksa apakah status server bernilai true atau kode HTTP bernilai 200 (Sukses)
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                // Jika server mereset status gagal, ambil pesan error atau berikan pesan default
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal memuat data produk."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 4. Menangani kesalahan dari sisi server (misal: Error 404, 500, dll.)
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 5. Menangani kesalahan koneksi jaringan (misal: Tidak ada kuota, internet mati, timeout)
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 6. Menangani kesalahan tak terduga lainnya (misal: Kesalahan parsing JSON, null pointer, dll.)
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * Fungsi untuk melakukan pencarian produk berdasarkan kata kunci (query) tertentu.
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState].
     *
     * @param query Kata kunci pencarian produk
     * @return Flow<ResultState<ProductResponse>> (Status Loading, Success, atau Error)
     */
    fun searchProducts(query: String): Flow<ResultState<ProductResponse>> = flow {
        // 1. Pancarkan status Loading saat proses pencarian ke server dimulai
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend pencarian dari ApiService
            val response = apiService.searchProducts(query)

            // 3. Periksa apakah respons berhasil
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                // Jika data pencarian kosong atau gagal ditemukan
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Pencarian produk tidak ditemukan."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 4. Menangani error HTTP dari server
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 5. Menangani error koneksi internet
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 6. Menangani error umum/tak terduga
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }
}