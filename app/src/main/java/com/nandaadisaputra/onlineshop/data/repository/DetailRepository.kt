package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException

/**
 * DetailRepository adalah kelas Repository yang bertugas sebagai perantara (Single Source of Truth)
 * antara ViewModel dengan sumber data jarak jauh (Remote API Service) khusus untuk halaman Detail Produk.
 */
class DetailRepository(private val apiService: ApiService) {

    /**
     * Fungsi untuk mengambil data detail produk berdasarkan ID produk tertentu.
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan (emit) [ResultState].
     *
     * @param productId ID unik dari produk yang ingin diambil detailnya
     * @return Flow<ResultState<DetailProductResponse>> (Loading, Success, atau Error)
     */
    fun getProductDetail(productId: Int): Flow<ResultState<DetailProductResponse>> = flow {
        // 1. Pancarkan status Loading saat permintaan data ke server baru dimulai
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend dari ApiService untuk mengambil data detail produk ke server
            val response = apiService.getProductDetail(productId)

            // 3. Memeriksa apakah status dari respons server bernilai true atau kode HTTP bernilai 200 (Sukses)
            if (response.status || response.code == 200) {
                // Pancarkan status Success beserta data respons dari server
                emit(ResultState.Success(response))
            } else {
                // Jika server mereset status gagal, ambil pesan error dari respons atau berikan pesan default
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal memuat detail produk."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 4. Menangani kesalahan dari sisi server (misal: Error 404 Not Found, 500 Internal Server Error)
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 5. Menangani kesalahan koneksi jaringan (misal: Tidak ada internet, timeout)
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 6. Menangani kesalahan tak terduga lainnya (misal: Parsing error, null pointer, dll.)
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }
}