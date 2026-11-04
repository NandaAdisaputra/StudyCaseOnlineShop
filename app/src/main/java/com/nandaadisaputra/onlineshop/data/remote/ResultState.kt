package com.nandaadisaputra.onlineshop.data.remote

/**
 * ResultState adalah kelas berpola Sealed Class (Sealed Class) yang bersifat generik (`<out T>`).
 * Kelas ini berfungsi sebagai pembungkus (Wrapper) reaktif untuk merepresentasikan berbagai macam
 * status atau kondisi dari sebuah proses asinkron (seperti saat mengambil data dari jaringan internet/API).
 *
 * MENGAPA MENGGUNAKAN SEALED CLASS?
 * Sealed class membatasi hirarki kelas keturunan (subclasses) hanya di dalam file ini saja.
 * Hal ini sangat berguna saat dipasangkan dengan percabangan `when` di Kotlin, di mana kompiler
 * akan memaksa kita untuk menangani seluruh kemungkinan kondisi (Loading, Success, Error) secara tuntas
 * tanpa memerlukan blok `else`.
 *
 * @param T Tipe data generik (bisa berupa Model Response apa saja, misal ProductResponse, CartResponse, dll.).
 * Keyword `out` (Covariance) berarti tipe T ini bersifat aman untuk dibaca (read-only).
 */
sealed class ResultState<out T> {

    /**
     * State 1: LOADING
     * Merepresentasikan kondisi saat proses permintaan (request) data ke server sedang dikirim
     * dan aplikasi masih menunggu balasan.
     * Menggunakan `object` (Singleton) karena status loading tidak membawa data apa pun di dalamnya,
     * sehingga cukup dibuat satu instance saja untuk menghemat memori.
     */
    object Loading : ResultState<Nothing>()

    /**
     * State 2: SUCCESS
     * Merepresentasikan kondisi saat proses permintaan data berhasil diselesaikan oleh server (sukses).
     * Menggunakan `data class` karena state ini membawa muatan data penting berupa objek hasil respons
     * dari server yang akan ditampilkan ke layar pengguna.
     *
     * @property data Objek data dengan tipe generik [T] yang berhasil diterima dari server.
     */
    data class Success<out T>(val data: T) : ResultState<T>()

    /**
     * State 3: ERROR
     * Merepresentasikan kondisi saat proses permintaan data mengalami kegagalan
     * (misal: koneksi internet mati, server error, atau autentikasi gagal).
     * Menggunakan `data class` untuk membawa informasi teks pesan kesalahan yang nantinya bisa
     * ditampilkan kepada pengguna melalui Toast atau Dialog.
     *
     * @property message Pesan teks penjelasan mengenai penyebab kegagalan.
     */
    data class Error(val message: String) : ResultState<Nothing>()
}