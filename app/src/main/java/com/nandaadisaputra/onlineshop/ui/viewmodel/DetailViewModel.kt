package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.repository.DetailRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi Repository dengan menyediakan ApiService langsung dari Application context
    private val repository: DetailRepository = DetailRepository(
        ApiConfig.provideApiService(application)
    )

    // MutableLiveData bersifat private agar hanya bisa diubah dari dalam ViewModel (Encapsulation)
    private val _detailResult = MutableLiveData<ResultState<DetailProductResponse>>()

    // LiveData publik yang bisa diamati (observe) oleh Activity/Fragment
    val detailResult: LiveData<ResultState<DetailProductResponse>> get() = _detailResult

    /**
     * Fungsi untuk mengambil data detail produk berdasarkan ID dari Repository.
     * Dijalankan di dalam viewModelScope agar coroutine otomatis berhenti saat ViewModel dihancurkan (mencegah memory leak).
     */
    fun fetchProductDetail(productId: Int) {
        viewModelScope.launch {
            // Memanggil fungsi dari repository yang mengembalikan Kotlin Flow
            repository.getProductDetail(productId).collectLatest { result ->
                // Mengirimkan hasil (Loading, Success, Error) ke Activity melalui LiveData
                _detailResult.value = result
            }
        }
    }
}