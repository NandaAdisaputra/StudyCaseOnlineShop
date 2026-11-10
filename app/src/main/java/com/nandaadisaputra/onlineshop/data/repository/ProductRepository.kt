package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiService
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import java.io.File

/**
 * ProductRepository adalah kelas Repository yang bertindak sebagai jembatan (Single Source of Truth)
 * antara ViewModel dengan sumber data jarak jauh (Remote API Service) untuk pengelolaan produk,
 * meliputi:
 * 1. Pengambilan daftar produk (pagination)
 * 2. Pencarian produk (search)
 * 3. Pengambilan detail produk (detail)
 * 4. Penambahan produk baru oleh Admin (create)
 * 5. Pembaruan produk oleh Admin (update)
 * 6. Penghapusan produk oleh Admin (delete)
 */
class ProductRepository(private val apiService: ApiService) {

    /**
     * 1. Fungsi untuk mengambil daftar produk dari server dengan sistem halaman (Pagination).
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
     * 2. Fungsi untuk melakukan pencarian produk berdasarkan kata kunci (query) tertentu.
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

    /**
     * 3. Fungsi untuk mengambil detail informasi produk berdasarkan ID.
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState].
     *
     * @param productId ID produk yang ingin diambil detailnya
     * @return Flow<ResultState<DetailProductResponse>> (Status Loading, Success, atau Error)
     */
    fun getProductDetail(productId: Int): Flow<ResultState<DetailProductResponse>> = flow {
        // 1. Pancarkan status Loading saat permintaan detail produk dimulai
        emit(ResultState.Loading)

        try {
            // 2. Memanggil fungsi suspend detail produk dari ApiService
            val response = apiService.getProductDetail(productId)

            // 3. Periksa apakah respons berhasil
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal memuat detail produk."
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

    /**
     * 4. Fungsi untuk menambahkan produk baru (Create Product - Admin).
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState].
     *
     * @param namaProduct Nama produk baru
     * @param harga Harga produk baru
     * @param deskripsi Deskripsi produk baru
     * @param imageFile File gambar produk yang dipilih dari galeri
     * @return Flow<ResultState<DetailProductResponse>> (Status Loading, Success, atau Error)
     */
    fun createProduct(
        namaProduct: String,
        harga: String,
        deskripsi: String,
        imageFile: File
    ): Flow<ResultState<DetailProductResponse>> = flow {
        // 1. Pancarkan status Loading saat proses tambah produk dimulai
        emit(ResultState.Loading)

        try {
            // 2. Mengonversi teks String menjadi RequestBody dan File gambar menjadi MultipartBody.Part
            val textMediaType = "text/plain".toMediaTypeOrNull()
            val namaBody = namaProduct.toRequestBody(textMediaType)
            val hargaBody = harga.toRequestBody(textMediaType)
            val deskripsiBody = deskripsi.toRequestBody(textMediaType)

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData(
                "gambar",
                imageFile.name,
                requestImageFile
            )

            // 3. Memanggil fungsi suspend createProduct dari ApiService
            val response = apiService.createProduct(
                namaProduct = namaBody,
                harga = hargaBody,
                deskripsi = deskripsiBody,
                gambar = imageMultipart
            )

            // 4. Periksa apakah respons berhasil (status true atau kode 201 Created / 200 OK)
            if (response.status || response.code == 201 || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal menambahkan produk."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 5. Menangani error HTTP dari server
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 6. Menangani error koneksi internet
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 7. Menangani error umum/tak terduga
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * 5. Fungsi untuk memperbarui data produk (Update Product - Admin).
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState].
     *
     * @param id ID produk yang akan diupdate
     * @param namaProduct Nama produk yang diperbarui
     * @param harga Harga produk yang diperbarui
     * @param deskripsi Deskripsi produk yang diperbarui
     * @param imageFile File gambar baru (bernilai null jika tidak mengganti gambar)
     * @return Flow<ResultState<DetailProductResponse>> (Status Loading, Success, atau Error)
     */
    fun updateProduct(
        id: Int,
        namaProduct: String,
        harga: String,
        deskripsi: String,
        imageFile: File?
    ): Flow<ResultState<DetailProductResponse>> = flow {
        // 1. Pancarkan status Loading saat proses update produk dimulai
        emit(ResultState.Loading)

        try {
            // 2. Mengonversi teks String menjadi RequestBody dan File gambar (jika ada) menjadi MultipartBody.Part
            val textMediaType = "text/plain".toMediaTypeOrNull()
            val idBody = id.toString().toRequestBody(textMediaType)
            val namaBody = namaProduct.toRequestBody(textMediaType)
            val hargaBody = harga.toRequestBody(textMediaType)
            val deskripsiBody = deskripsi.toRequestBody(textMediaType)

            val imageMultipart = imageFile?.let { file ->
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("gambar", file.name, requestImageFile)
            }

            // 3. Memanggil fungsi suspend updateProduct dari ApiService
            val response = apiService.updateProduct(
                id = idBody,
                namaProduct = namaBody,
                harga = hargaBody,
                deskripsi = deskripsiBody,
                gambar = imageMultipart
            )

            // 4. Periksa apakah respons berhasil
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal memperbarui produk."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 5. Menangani error HTTP dari server
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 6. Menangani error koneksi internet
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 7. Menangani error umum/tak terduga
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }

    /**
     * 6. Fungsi untuk menghapus produk berdasarkan ID (Delete Product - Admin).
     * Mengembalikan data dalam bentuk Kotlin Coroutines [Flow] yang memancarkan [ResultState].
     *
     * @param productId ID produk yang akan dihapus dari server
     * @return Flow<ResultState<DetailProductResponse>> (Status Loading, Success, atau Error)
     */
    fun deleteProduct(productId: Int): Flow<ResultState<DetailProductResponse>> = flow {
        // 1. Pancarkan status Loading saat proses hapus produk ke server dimulai
        emit(ResultState.Loading)

        try {
            // 2. Mengonversi ID produk menjadi RequestBody untuk dikirim melalui form-data
            val textMediaType = "text/plain".toMediaTypeOrNull()
            val idBody = productId.toString().toRequestBody(textMediaType)

            // 3. Memanggil fungsi suspend deleteProduct dari ApiService
            val response = apiService.deleteProduct(
                queryId = productId,
                bodyId = idBody
            )

            // 4. Periksa apakah respons penghapusan berhasil
            if (response.status || response.code == 200) {
                emit(ResultState.Success(response))
            } else {
                // Jika produk gagal dihapus atau tidak ditemukan
                val errorMessage = if (!response.message.isNullOrEmpty()) {
                    response.message
                } else {
                    "Gagal menghapus produk."
                }
                emit(ResultState.Error(errorMessage))
            }
        } catch (e: HttpException) {
            // 5. Menangani error HTTP dari server (misal: Error 404 Produk tidak ditemukan)
            val errorMessage = "Terjadi kesalahan pada server (HTTP ${e.code()}): ${e.message()}"
            emit(ResultState.Error(errorMessage))
        } catch (e: IOException) {
            // 6. Menangani error koneksi internet
            emit(ResultState.Error("Koneksi internet bermasalah. Periksa kembali sambungan Anda!"))
        } catch (e: Exception) {
            // 7. Menangani error umum/tak terduga
            emit(ResultState.Error("Terjadi kesalahan yang tidak diketahui: ${e.localizedMessage ?: "Kesalahan sistem"}"))
        }
    }
}