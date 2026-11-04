package com.nandaadisaputra.onlineshop.data.model

/**
 * ProductResponse adalah kelas data (Data Class) utama yang merepresentasikan
 * struktur JSON dari respons server secara keseluruhan (Root Response).
 */
data class ProductResponse(
    val status: Boolean,     // Status keberhasilan request dari server (true jika sukses, false jika gagal)
    val code: Int,           // Kode HTTP status dari server (contoh: 200 untuk sukses, 404/500 untuk error)
    val message: String,     // Pesan informasi dari server (contoh: "Success get products")
    val data: ProductData?   // Objek data utama pembungkus produk dan pagination (bisa null jika terjadi error)
)

/**
 * ProductData adalah kelas data yang membungkus payload informasi produk,
 * status pagination, kata kunci pencarian, serta daftar list produk.
 */
data class ProductData(
    val pagination: Pagination?,       // Informasi navigasi halaman (paging) untuk infinite scroll
    val keyword: String?,              // Kata kunci pencarian jika fitur search sedang digunakan
    val total_found: Int?,             // Total produk yang ditemukan berdasarkan kata kunci pencarian
    val products: List<ProductItem>    // Daftar (list) item produk yang dikembalikan oleh server
)

/**
 * Pagination adalah kelas data yang menampung informasi navigasi halaman
 * untuk mengontrol batasan pemuatan data (load more).
 */
data class Pagination(
    val current_page: Int, // Posisi halaman aktif saat ini (contoh: halaman 1)
    val per_page: Int,     // Jumlah maksimal item data per halaman (limit per request)
    val total_data: Int,   // Total keseluruhan data produk yang tersedia di database server
    val total_pages: Int   // Total keseluruhan halaman maksimal yang dapat dimuat
)

/**
 * ProductItem adalah kelas data yang merepresentasikan entitas tunggal dari sebuah produk
 * beserta atribut lengkapnya yang diambil dari database server.
 */
data class ProductItem(
    val id: Int,              // ID unik pembeda untuk setiap produk (digunakan untuk DiffUtil dan detail)
    val nama_product: String, // Nama atau judul produk
    val harga: String,        // Harga produk (disimpan dalam bentuk String atau angka sesuai format API)
    val deskripsi: String,    // Keterangan atau deskripsi lengkap mengenai produk
    val gambar: String,       // URL atau tautan gambar produk untuk dimuat menggunakan Glide/Coil
    val created_at: String    // Waktu pencatatan/pembuatan data produk di server (timestamp)
)