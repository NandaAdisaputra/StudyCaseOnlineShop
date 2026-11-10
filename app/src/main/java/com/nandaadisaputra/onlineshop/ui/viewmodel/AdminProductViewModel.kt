package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.model.ProductItem
import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.ProductRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

/**
 * AdminProductViewModel adalah kelas pengelola logika tampilan dan state (ViewModel dalam arsitektur MVVM)
 * khusus untuk fitur-fitur manajemen produk oleh Administrator (CRUD & Pencarian).
 *
 * Kelas ini bertanggung jawab untuk:
 * 1. Mengambil daftar produk dari Repository dengan dukungan Pagination (Infinite Scroll).
 * 2. Menangani fitur Pencarian produk (Search) di Dashboard Admin.
 * 3. Mengambil informasi Detail Produk berdasarkan ID.
 * 4. Menambahkan produk baru beserta unggahan file gambar (Create Product).
 * 5. Memperbarui data produk dan gambar (Update Product).
 * 6. Menghapus produk berdasarkan ID (Delete Product).
 *
 * Menggunakan [AndroidViewModel] agar dapat mengakses Application context untuk inisialisasi
 * [ApiConfig.provideApiService] yang memerlukan akses Token dari SharedPreferences melalui AuthInterceptor.
 */
class AdminProductViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi ProductRepository dengan menyediakan ApiService yang dilengkapi AuthInterceptor.
    private val repository: ProductRepository = ProductRepository(
        ApiConfig.provideApiService(application)
    )

    // =========================================================================================
    // 2. ENKAPSULASI LIVE DATA (Backing Property Pattern)
    // =========================================================================================

    // State untuk Daftar Produk & Pencarian (digunakan di AdminDashboardActivity)
    private val _productResult = MutableLiveData<ResultState<ProductResponse>>()
    val productResult: LiveData<ResultState<ProductResponse>> get() = _productResult

    // State untuk Detail Produk (digunakan di DetailProductAdminActivity)
    private val _detailResult = MutableLiveData<ResultState<DetailProductResponse>>()
    val detailResult: LiveData<ResultState<DetailProductResponse>> get() = _detailResult

    // State untuk Aksi Tambah & Edit Produk (digunakan di AddProductActivity & EditProductActivity)
    private val _actionResult = MutableLiveData<ResultState<DetailProductResponse>>()
    val actionResult: LiveData<ResultState<DetailProductResponse>> get() = _actionResult

    // State khusus untuk Aksi Hapus Produk (digunakan di DetailProductAdminActivity)
    private val _deleteResult = MutableLiveData<ResultState<DetailProductResponse>>()
    val deleteResult: LiveData<ResultState<DetailProductResponse>> get() = _deleteResult


    // =========================================================================================
    // 3. VARIABEL PENGELOLA PAGINATION & PENAMPUNG DATA LIST
    // =========================================================================================
    var currentPage = 1
    var totalPages = 1
    var isLoadingMore = false

    // Penampung list produk secara kumulatif untuk mendukung pagination
    private val productList = mutableListOf<ProductItem>()


    // =========================================================================================
    // 4. FUNGSI DAFTAR PRODUK (PAGINATION) & PENCARIAN (SEARCH)
    // =========================================================================================

    /**
     * Fungsi untuk mengambil data produk dari server berdasarkan halaman (page) dan limit.
     *
     * @param page Halaman data yang ingin diambil dari server
     * @param limit Jumlah maksimal item produk per halaman
     * @param isLoadMore Bernilai `true` jika dipanggil saat pengguna menggulir ke bawah (pagination)
     */
    fun fetchProducts(page: Int, limit: Int, isLoadMore: Boolean = false) {
        if (isLoadingMore) return

        if (isLoadMore) {
            isLoadingMore = true
        }

        viewModelScope.launch {
            repository.getProducts(page, limit).collectLatest { result ->
                when (result) {
                    is ResultState.Loading -> {
                        if (!isLoadMore) {
                            _productResult.value = ResultState.Loading
                        }
                    }

                    is ResultState.Success -> {
                        val pagination = result.data.data?.pagination
                        totalPages = pagination?.total_pages ?: 1
                        currentPage = pagination?.current_page ?: page

                        val fetchedProducts = result.data.data?.products.orEmpty()

                        if (isLoadMore) {
                            productList.addAll(fetchedProducts)
                        } else {
                            productList.clear()
                            productList.addAll(fetchedProducts)
                        }

                        isLoadingMore = false

                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        _productResult.value = ResultState.Success(updatedResponse)
                    }

                    is ResultState.Error -> {
                        isLoadingMore = false
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Fungsi untuk melakukan pencarian produk berdasarkan kata kunci (query).
     *
     * @param query Kata kunci pencarian produk yang diketik admin
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            repository.searchProducts(query).collectLatest { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _productResult.value = ResultState.Loading
                    }

                    is ResultState.Success -> {
                        val fetchedProducts = result.data.data?.products.orEmpty()

                        productList.clear()
                        productList.addAll(fetchedProducts)

                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        _productResult.value = ResultState.Success(updatedResponse)
                    }

                    is ResultState.Error -> {
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }


    // =========================================================================================
    // 5. FUNGSI DETAIL PRODUK (READ DETAIL)
    // =========================================================================================

    /**
     * Fungsi untuk mengambil informasi detail satu produk berdasarkan ID.
     *
     * @param productId ID unik dari produk yang dipilih
     */
    fun fetchProductDetail(productId: Int) {
        viewModelScope.launch {
            repository.getProductDetail(productId).collectLatest { result ->
                _detailResult.value = result
            }
        }
    }


    // =========================================================================================
    // 6. FUNGSI TAMBAH PRODUK BARU (CREATE PRODUCT)
    // =========================================================================================

    /**
     * Fungsi untuk mengirim data produk baru beserta gambar ke server.
     *
     * @param nama Nama produk baru
     * @param harga Harga produk
     * @param deskripsi Deskripsi lengkap produk
     * @param imageFile File gambar produk yang dipilih dari galeri perangkat
     */
    fun createProduct(nama: String, harga: String, deskripsi: String, imageFile: File) {
        viewModelScope.launch {
            repository.createProduct(
                namaProduct = nama,
                harga = harga,
                deskripsi = deskripsi,
                imageFile = imageFile
            ).collectLatest { result ->
                _actionResult.value = result
            }
        }
    }


    // =========================================================================================
    // 7. FUNGSI PERBARUI PRODUK (UPDATE PRODUCT)
    // =========================================================================================

    /**
     * Fungsi untuk memperbarui data produk yang sudah ada di server.
     *
     * @param id ID produk yang sedang diedit
     * @param nama Nama produk yang diperbarui
     * @param harga Harga produk yang diperbarui
     * @param deskripsi Deskripsi produk yang diperbarui
     * @param imageFile File gambar baru (bernilai `null` jika admin tidak mengganti gambar)
     */
    fun updateProduct(
        id: Int,
        nama: String,
        harga: String,
        deskripsi: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            repository.updateProduct(
                id = id,
                namaProduct = nama,
                harga = harga,
                deskripsi = deskripsi,
                imageFile = imageFile
            ).collectLatest { result ->
                _actionResult.value = result
            }
        }
    }


    // =========================================================================================
    // 8. FUNGSI HAPUS PRODUK (DELETE PRODUCT)
    // =========================================================================================

    /**
     * Fungsi untuk menghapus produk dari server berdasarkan ID.
     *
     * @param productId ID produk yang akan dihapus
     */
    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId).collectLatest { result ->
                _deleteResult.value = result
            }
        }
    }
}