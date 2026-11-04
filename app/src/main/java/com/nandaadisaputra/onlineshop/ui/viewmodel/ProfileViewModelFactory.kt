package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.repository.ProfileRepository

/**
 * ProfileViewModelFactory adalah kelas pabrik (Factory Pattern) yang bertugas untuk membuat
 * instance dari [ProfileViewModel].
 *
 * MENGAPA KITA MEMBUTUHKAN FACTORY INI?
 * Secara bawaan (default), Android System hanya bisa membuat ViewModel yang memiliki konstruktor kosong
 * (tanpa parameter). Karena [ProfileViewModel] membutuhkan parameter berupa [ProfileRepository]
 * di konstruktornya (`class ProfileViewModel(private val repository: ProfileRepository)`), maka kita
 * wajib membuat kelas Factory ini yang mewarisi [ViewModelProvider.NewInstanceFactory] untuk memberi tahu
 * Android bagaimana cara merakit dan membuat objek ProfileViewModel tersebut.
 *
 * @property repository Objek ProfileRepository yang akan disuntikkan ke dalam ProfileViewModel.
 */
class ProfileViewModelFactory(private val repository: ProfileRepository) :
    ViewModelProvider.NewInstanceFactory() {

    /**
     * Anotasi @Suppress("UNCHECKED_CAST") digunakan untuk menghilangkan peringatan (warning) dari compiler
     * Kotlin saat kita mengubah (casting) tipe objek `ProfileViewModel` menjadi tipe generik `T` (`as T`).
     * Casting ini sudah dijamin aman karena kita telah memeriksanya terlebih dahulu menggunakan `isAssignableFrom`.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 1. Memeriksa apakah kelas ViewModel yang diminta oleh Activity/Fragment adalah ProfileViewModel
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            // 2. Jika benar, buat objek ProfileViewModel dengan memasukkan repository ke konstruktornya,
            // lalu kembalikan sebagai tipe generik T.
            return ProfileViewModel(repository) as T
        }

        // 3. Jika kelas ViewModel yang diminta tidak dikenali oleh Factory ini, lemparkan pesan error
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    /**
     * Companion Object berfungsi seperti method 'static' di Java, sehingga fungsi `getInstance()`
     * dapat dipanggil langsung dari Activity/Fragment (contoh: `ProfileViewModelFactory.getInstance(this)`)
     * tanpa harus menginisialisasi objek ProfileViewModelFactory terlebih dahulu.
     *
     * Bagian ini menerapkan konsep Dependency Injection (Penyuntikan Ketergantungan) secara manual.
     */
    companion object {
        /**
         * Fungsi penyedia (Provider) untuk merakit seluruh komponen yang dibutuhkan oleh ProfileViewModel
         * secara berurutan: Context -> ApiService (dengan AuthInterceptor) -> ProfileRepository -> Factory[cite: 4, 7].
         *
         * @param context Context dari Activity/Fragment yang dibutuhkan untuk mengambil Token dari SharedPreferences[cite: 4, 7].
         * @return Instance dari [ProfileViewModelFactory] yang siap digunakan oleh delegasi `by viewModels`.
         */
        fun getInstance(context: Context): ProfileViewModelFactory {
            // Langkah 1: Membuat instance ApiService dengan mengirimkan Context agar AuthInterceptor
            // secara otomatis menyisipkan Header "Authorization: Bearer <token>" di setiap request API[cite: 4, 7].
            val apiService = ApiConfig.provideApiService(context)

            // Langkah 2: Membuat instance ProfileRepository dengan memasukkan apiService di atas.
            val repository = ProfileRepository(apiService)

            // Langkah 3: Mengembalikan objek ProfileViewModelFactory yang sudah membawa repository.
            return ProfileViewModelFactory(repository)
        }
    }
}