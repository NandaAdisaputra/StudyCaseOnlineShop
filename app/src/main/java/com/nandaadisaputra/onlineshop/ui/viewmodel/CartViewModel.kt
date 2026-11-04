package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.CartListResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.CartRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * CartViewModel adalah kelas pengelola data dan logika tampilan (ViewModel dalam arsitektur MVVM)
 * khusus untuk halaman Keranjang / Riwayat Pesanan (CartFragment).
 *
 * Kelas ini mewarisi (inherit) [AndroidViewModel], bukan ViewModel biasa, karena kita membutuhkan
 * objek [Application] (Application Context) untuk menginisialisasi ApiConfig yang memerlukan akses
 * ke SharedPreferences melalui AuthInterceptor[cite: 4, 7].
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inisialisasi CartRepository dengan mengirimkan instance ApiService.
    // ApiConfig.provideApiService(application) akan otomatis menyisipkan Bearer Token pengguna
    // ke setiap request HTTP melalui AuthInterceptor[cite: 4, 7].
    private val repository: CartRepository = CartRepository(
        ApiConfig.provideApiService(application)
    )

    // 2. Penerapan prinsip Encapsulation (Enkapsulasi Data) pada LiveData:
    // _cartListResult bersifat 'private' dan 'Mutable' (bisa diubah nilainya), sehingga hanya
    // CartViewModel ini saja yang berhak memodifikasi state/data keranjang.
    private val _cartListResult = MutableLiveData<ResultState<CartListResponse>>()

    // cartListResult bersifat 'public' dan bertipe 'LiveData' biasa (Read-Only / tidak bisa diubah dari luar).
    // Variabel inilah yang akan diamati (di-observe) oleh CartFragment untuk memperbarui tampilan UI.
    val cartListResult: LiveData<ResultState<CartListResponse>> get() = _cartListResult

    /**
     * Fungsi untuk memicu pengambilan daftar pesanan keranjang dari server melalui Repository.
     */
    fun fetchCartList() {
        // 3. Menjalankan proses pemanggilan jaringan di dalam 'viewModelScope.launch'.
        // viewModelScope adalah CoroutineScope bawaan yang terikat dengan siklus hidup (Lifecycle) ViewModel.
        // Jika pengguna keluar dari halaman dan ViewModel dihancurkan (onCleared), maka proses request
        // otomatis dibatalkan sehingga mencegah kebocoran memori (Memory Leak).
        viewModelScope.launch {

            // 4. Memanggil fungsi getCartList() dari repository yang mengembalikan aliran data (Flow).
            // 'collectLatest' digunakan untuk menangkap setiap pancaran status (Loading -> Success / Error)[cite: 6].
            // Keunggulan 'collectLatest': jika fungsi fetchCartList() dipanggil ulang sebelum request
            // sebelumnya selesai, maka request lama otomatis dibatalkan dan diganti dengan yang terbaru.
            repository.getCartList().collectLatest { result ->
                // Memperbarui nilai LiveData agar CartFragment yang sedang mengamati (observe)
                // langsung merespons perubahan status tersebut di layar.
                _cartListResult.value = result
            }
        }
    }
}