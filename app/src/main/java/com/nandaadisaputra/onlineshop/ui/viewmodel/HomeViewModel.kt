package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.ProductItem
import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.ProductRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * HomeViewModel bertanggung jawab untuk mengelola data (State Management) pada HomeFragment,
 * termasuk mengambil data produk dari Repository, menangani Pagination, dan fitur Pencarian (Search).
 * Menggunakan AndroidViewModel agar dapat mengakses Application context untuk inisialisasi API/AuthInterceptor.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi Repository produk dengan menyediakan ApiService dan token otomatis dari interceptor
    private val repository: ProductRepository = ProductRepository(
        ApiConfig.provideApiService(application)
    )

    // LiveData privat untuk mengubah nilai state di dalam ViewModel
    private val _productResult = MutableLiveData<ResultState<ProductResponse>>()

    // LiveData publik yang hanya bisa dibaca (di-observe) oleh Fragment/Activity
    val productResult: LiveData<ResultState<ProductResponse>> get() = _productResult

    // Variabel untuk mengelola mekanisme Pagination (Infinite Scroll)
    var currentPage = 1
    var totalPages = 1
    var isLoadingMore = false

    // Menyimpan list produk secara kumulatif (gabungan halaman 1, halaman 2, dst. untuk load more)
    private val productList = mutableListOf<ProductItem>()

    /**
     * Fungsi untuk mengambil data produk dari server berdasarkan halaman (page) dan limit.
     * @param page Halaman yang ingin diambil
     * @param limit Jumlah item per halaman
     * @param isLoadMore Bernilai true jika dipanggil saat scroll ke bawah (pagination)
     */
    fun fetchProducts(page: Int, limit: Int, isLoadMore: Boolean = false) {
        if (isLoadingMore) return // Mencegah pemanggilan ganda jika proses load more sedang berjalan

        if (isLoadMore) {
            isLoadingMore = true
        }

        viewModelScope.launch {
            repository.getProducts(page, limit).collectLatest { result ->
                when (result) {
                    is ResultState.Loading -> {
                        // Jika bukan proses load more (artinya first load atau refresh), tampilkan loading utama
                        if (!isLoadMore) {
                            _productResult.value = ResultState.Loading
                        }
                    }
                    is ResultState.Success -> {
                        // Mengambil data informasi pagination dari respons server
                        val pagination = result.data.data?.pagination
                        totalPages = pagination?.total_pages ?: 1
                        currentPage = pagination?.current_page ?: page

                        val fetchedProducts = result.data.data?.products.orEmpty()

                        if (isLoadMore) {
                            // Jika load more, tambahkan produk baru ke dalam list yang sudah ada
                            productList.addAll(fetchedProducts)
                        } else {
                            // Jika bukan load more (misal halaman 1 / refresh), bersihkan list lama lalu isi dengan data baru
                            productList.clear()
                            productList.addAll(fetchedProducts)
                        }
                        isLoadingMore = false

                        // Membuat salinan respons dengan list produk yang sudah kumulatif
                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        // Kirim data sukses ke UI melalui LiveData
                        _productResult.value = ResultState.Success(updatedResponse)
                    }
                    is ResultState.Error -> {
                        isLoadingMore = false
                        // Kirim pesan error ke UI melalui LiveData
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Fungsi untuk melakukan pencarian produk berdasarkan kata kunci (query).
     * Hasil pencarian akan mereset productList agar hanya menampilkan hasil yang sesuai dengan query.
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            repository.searchProducts(query).collectLatest { result ->
                when (result) {
                    is ResultState.Loading -> {
                        // Tampilkan loading saat proses pencarian berlangsung
                        _productResult.value = ResultState.Loading
                    }
                    is ResultState.Success -> {
                        val fetchedProducts = result.data.data?.products.orEmpty()

                        // Bersihkan list utama dan masukkan hasil pencarian
                        productList.clear()
                        productList.addAll(fetchedProducts)

                        // Kirim hasil pencarian ke UI
                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        _productResult.value = ResultState.Success(updatedResponse)
                    }
                    is ResultState.Error -> {
                        // Kirim pesan error pencarian jika gagal
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }
}