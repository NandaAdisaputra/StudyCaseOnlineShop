package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.CartResponse
import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.DetailRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * DetailViewModel adalah kelas pengelola data dan logika tampilan (ViewModel dalam arsitektur MVVM)
 * yang digunakan khusus pada halaman DetailUserActivity.
 *
 * Kelas ini mewarisi [AndroidViewModel], bukan ViewModel biasa, karena kita membutuhkan
 * objek [Application] (Application Context) untuk menginisialisasi ApiService yang dilengkapi
 * dengan AuthInterceptor guna menyisipkan Bearer Token secara otomatis.
 */
class DetailViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi DetailRepository dengan menyediakan ApiService langsung dari Application context.
    // Repository bertindak sebagai perantara komunikasi data antara ViewModel dengan server.
    private val repository: DetailRepository = DetailRepository(
        ApiConfig.provideApiService(application)
    )

    // =========================================================================================
    // 2. LIVE DATA UNTUK DETAIL PRODUK (Digunakan saat halaman pertama kali dibuka)
    // =========================================================================================

    // Backing Property (_detailResult): Bersifat 'private' dan 'MutableLiveData' (bisa diubah nilainya).
    // Hanya ViewModel ini saja yang berhak mengubah state detail produk (Penerapan prinsip Encapsulation).
    private val _detailResult = MutableLiveData<ResultState<DetailProductResponse>>()

    // Exposed LiveData (detailResult): Bersifat 'public' dan bertipe 'LiveData' (Read-Only).
    // Variabel inilah yang diamati (di-observe) oleh DetailUserActivity untuk merender data produk.
    val detailResult: LiveData<ResultState<DetailProductResponse>> get() = _detailResult


    // =========================================================================================
    // 3. LIVE DATA UNTUK PESAN PRODUK / ADD TO CART (Digunakan saat tombol BELI diklik)
    // =========================================================================================

    // Backing Property (_cartResult): Menyimpan status proses pengiriman produk ke keranjang secara internal.
    private val _cartResult = MutableLiveData<ResultState<CartResponse>>()

    // Exposed LiveData (cartResult): LiveData publik Read-Only yang diamati oleh DetailUserActivity
    // untuk menampilkan pesan sukses/gagal saat produk berhasil dimasukkan ke keranjang.
    val cartResult: LiveData<ResultState<CartResponse>> get() = _cartResult


    // =========================================================================================
    // 4. FUNGSI AKSI (EVENT HANDLER DARI UI KE REPOSITORY)
    // =========================================================================================

    /**
     * Fungsi untuk mengambil data detail produk berdasarkan ID dari Repository.
     *
     * @param productId ID unik produk yang dikirim dari halaman sebelumnya melalui Intent.
     */
    fun fetchProductDetail(productId: Int) {
        // Dijalankan di dalam 'viewModelScope.launch' (CoroutineScope bawaan ViewModel).
        // Coroutine ini otomatis dibatalkan saat ViewModel dihancurkan (onCleared),
        // sehingga mencegah terjadinya kebocoran memori (Memory Leak) atau crash saat proses jaringan selesai
        // ketika Activity sudah ditutup[cite: 12].
        viewModelScope.launch {
            // Memanggil fungsi getProductDetail dari repository yang mengembalikan aliran data (Flow),
            // lalu 'collectLatest' menangkap setiap perubahan status (Loading -> Success / Error).
            repository.getProductDetail(productId).collectLatest { result ->
                // Memperbarui nilai LiveData agar Activity yang sedang mengobserve langsung memperbarui tampilan UI[cite: 12].
                _detailResult.value = result
            }
        }
    }

    /**
     * Fungsi baru untuk mengirim data Pesan Produk / Beli ke server (Add to Cart).
     * Mengirimkan parameter productId dan quantity ke repository.
     *
     * @param productId ID produk yang ingin dibeli
     * @param quantity Jumlah kuantitas produk yang dipesan (misal: 1
     */
    fun addToCart(productId: Int, quantity: Int) {
        // Menjalankan coroutine asinkron di latar belakang (background thread)
        viewModelScope.launch {
            // Memanggil fungsi addToCart di repository dan menangkap status pancaran Flow-nya (Loading -> Success / Error)
            repository.addToCart(productId, quantity).collectLatest { result ->
                // Meneruskan hasil respons server ke LiveData _cartResult agar Activity bisa menampilkan Toast
                _cartResult.value = result
            }
        }
    }
}