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
 * HomeViewModel adalah kelas pengelola logika tampilan dan state (ViewModel dalam arsitektur MVVM)
 * untuk halaman HomeFragment.
 *
 * Kelas ini bertanggung jawab untuk:
 * 1. Mengambil data produk dari Repository secara asinkron.
 * 2. Mengelola mekanisme Pagination (muat halaman berikutnya / Infinite Scroll).
 * 3. Menangani fitur Pencarian produk (Search).
 *
 * Menggunakan [AndroidViewModel] agar dapat mengakses Application context untuk inisialisasi
 * [ApiConfig.provideApiService] yang memerlukan akses Token dari SharedPreferences melalui AuthInterceptor.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi ProductRepository dengan menyediakan ApiService yang dilengkapi AuthInterceptor.
    private val repository: ProductRepository = ProductRepository(
        ApiConfig.provideApiService(application)
    )

    // =========================================================================================
    // 2. ENKAPSULASI LIVE DATA (Backing Property Pattern)
    // =========================================================================================

    // _productResult bersifat 'private' dan 'MutableLiveData' (hanya ViewModel ini yang bisa mengubah nilainya).
    private val _productResult = MutableLiveData<ResultState<ProductResponse>>()

    // productResult bersifat 'public' dan bertipe 'LiveData' (Read-Only).
    // Variabel inilah yang diamati (di-observe) oleh HomeFragment untuk memperbarui tampilan UI.
    val productResult: LiveData<ResultState<ProductResponse>> get() = _productResult


    // =========================================================================================
    // 3. VARIABEL PENGELOLA PAGINATION & PENAMPUNG DATA
    // =========================================================================================
    var currentPage = 1
    var totalPages = 1
    var isLoadingMore =
        false // Flag penanda agar tidak terjadi pemanggilan data ganda saat proses load more berjalan

    // Penampung list produk secara kumulatif (gabungan produk halaman 1, halaman 2, dst. untuk pagination).
    private val productList = mutableListOf<ProductItem>()


    // =========================================================================================
    // 4. FUNGSI UTAMA PENGAMBILAN DATA PRODUK (DENGAN SUPPORT PAGINATION)
    // =========================================================================================

    /**
     * Fungsi untuk mengambil data produk dari server berdasarkan halaman (page) dan limit.
     *
     * @param page Halaman data yang ingin diambil dari server
     * @param limit Jumlah maksimal item produk per halaman
     * @param isLoadMore Bernilai `true` jika fungsi ini dipanggil saat pengguna menggulir ke bawah (pagination)
     */
    fun fetchProducts(page: Int, limit: Int, isLoadMore: Boolean = false) {
        // Mencegah pemanggilan ganda jika proses load more halaman sebelumnya belum selesai
        if (isLoadingMore) return

        if (isLoadMore) {
            isLoadingMore = true
        }

        // Menjalankan coroutine di dalam viewModelScope (otomatis dibatalkan saat ViewModel dihancurkan)
        viewModelScope.launch {
            // Memanggil repository dan mendengarkan aliran data (Flow) menggunakan collectLatest
            repository.getProducts(page, limit).collectLatest { result ->
                when (result) {
                    // Kondisi 1: LOADING (Proses permintaan data sedang berlangsung)
                    is ResultState.Loading -> {
                        // Jika bukan proses load more (artinya first load / refresh), tampilkan loading utama di UI
                        if (!isLoadMore) {
                            _productResult.value = ResultState.Loading
                        }
                    }

                    // Kondisi 2: SUCCESS (Data produk berhasil diterima dari server)
                    is ResultState.Success -> {
                        // Mengambil informasi data pagination dari respons server
                        val pagination = result.data.data?.pagination
                        totalPages = pagination?.total_pages ?: 1
                        currentPage = pagination?.current_page ?: page

                        // Mengambil daftar produk baru dari respons (gunakan orEmpty() untuk mencegah null)
                        val fetchedProducts = result.data.data?.products.orEmpty()

                        if (isLoadMore) {
                            // Jika load more: Tambahkan produk baru ke dalam daftar yang sudah ada sebelumnya
                            productList.addAll(fetchedProducts)
                        } else {
                            // Jika bukan load more (first load / pull-to-refresh): Bersihkan list lama, lalu isi data baru
                            productList.clear()
                            productList.addAll(fetchedProducts)
                        }

                        // Kembalikan status isLoadingMore menjadi false agar bisa melakukan scroll berikutnya
                        isLoadingMore = false

                        // Membuat salinan objek respons dengan list produk yang sudah kumulatif (gabungan halaman)
                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        // Kirim data sukses ke Fragment melalui LiveData
                        _productResult.value = ResultState.Success(updatedResponse)
                    }

                    // Kondisi 3: ERROR (Terjadi kendala jaringan atau server error)
                    is ResultState.Error -> {
                        isLoadingMore = false
                        // Kirim pesan error ke Fragment melalui LiveData
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }


    // =========================================================================================
    // 5. FUNGSI PENCARIAN PRODUK (SEARCH)
    // =========================================================================================

    /**
     * Fungsi untuk melakukan pencarian produk berdasarkan kata kunci (query).
     * Hasil pencarian akan mereset productList agar hanya menampilkan produk yang sesuai dengan query.
     *
     * @param query Kata kunci pencarian produk yang diketik pengguna.
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            repository.searchProducts(query).collectLatest { result ->
                when (result) {
                    is ResultState.Loading -> {
                        // Tampilkan indikator loading saat proses pencarian berlangsung
                        _productResult.value = ResultState.Loading
                    }

                    is ResultState.Success -> {
                        val fetchedProducts = result.data.data?.products.orEmpty()

                        // Bersihkan list utama, lalu masukkan khusus hasil pencarian yang ditemukan
                        productList.clear()
                        productList.addAll(fetchedProducts)

                        // Bungkus data hasil pencarian ke dalam struktur respons yang sama
                        val updatedData = result.data.data?.copy(products = productList.toList())
                        val updatedResponse = result.data.copy(data = updatedData)

                        // Kirim hasil sukses pencarian ke UI
                        _productResult.value = ResultState.Success(updatedResponse)
                    }

                    is ResultState.Error -> {
                        // Kirim pesan error pencarian jika gagal atau produk tidak ditemukan
                        _productResult.value = ResultState.Error(result.message)
                    }
                }
            }
        }
    }
}