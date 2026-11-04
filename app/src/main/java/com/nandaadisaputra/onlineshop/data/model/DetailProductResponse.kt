package com.nandaadisaputra.onlineshop.data.model

import com.google.gson.annotations.SerializedName

/**
 * DetailProductResponse adalah kelas data (Data Class) utama yang merepresentasikan
 * struktur respons JSON dari server ketika aplikasi meminta detail data suatu produk tertentu.
 */
data class DetailProductResponse(

    // @SerializedName digunakan untuk mencocokkan nama key pada JSON dari server
    // dengan variabel di dalam kode Kotlin (sangat berguna jika menggunakan ProGuard/Minification).
    @SerializedName("status")
    val status: Boolean, // Status keberhasilan request (true jika sukses, false jika gagal)

    @SerializedName("code")
    val code: Int, // Kode HTTP Status dari server (misal: 200 untuk OK, 404 untuk Not Found)

    @SerializedName("message")
    val message: String, // Pesan informasi dari server (misal: "Detail product found")

    @SerializedName("data")
    val data: ProductDetailItem? // Objek tunggal penampung detail produk (bisa null jika data tidak ditemukan)
)

/**
 * ProductDetailItem adalah kelas data yang merepresentasikan informasi detail
 * dari sebuah produk spesifik berdasarkan ID-nya.
 */
data class ProductDetailItem(

    @SerializedName("id")
    val id: Int, // ID unik pembeda untuk setiap produk

    @SerializedName("nama_product")
    val nama_product: String, // Nama lengkap produk

    @SerializedName("harga")
    val harga: String, // Harga produk

    @SerializedName("deskripsi")
    val deskripsi: String, // Keterangan atau spesifikasi lengkap produk

    @SerializedName("gambar")
    val gambar: String, // URL tautan gambar produk untuk dimuat menggunakan Glide/Coil

    @SerializedName("created_at")
    val created_at: String // Tanggal dan waktu saat data produk pertama kali dibuat di server
)