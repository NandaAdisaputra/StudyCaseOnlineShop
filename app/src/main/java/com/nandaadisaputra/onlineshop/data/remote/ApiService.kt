package com.nandaadisaputra.onlineshop.data.remote

import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.CartListResponse
import com.nandaadisaputra.onlineshop.data.model.CartResponse
import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ApiService adalah sebuah interface (kontrak) yang mendefinisikan seluruh daftar endpoint API
 * (alamat URL server, metode HTTP, parameter kiriman, dan tipe data respons) yang akan dipanggil
 * oleh aplikasi menggunakan library Retrofit.
 */
interface ApiService {

    /**
     * Endpoint 1: Pendaftaran Akun Pengguna Baru (Register).
     * - @FormUrlEncoded & @Field: Digunakan untuk mengirim data berbentuk pasangan key-value (form URL encoded) di dalam body HTTP POST.
     */
    @FormUrlEncoded
    @POST("OnlineShop/auth/register.php")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("alamat") alamat: String,
        @Field("no_whatsapp") noWhatsApp: String,
        @Field("password") password: String
    ): AuthResponse<UserData>

    /**
     * Endpoint 2: Masuk Akun (Login).
     * Mengirimkan username dan password untuk memvalidasi identitas dan mendapatkan Token autentikasi dari server.
     */
    @FormUrlEncoded
    @POST("OnlineShop/auth/login.php")
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponse<UserData>

    /**
     * Endpoint 3: Keluar Akun (Logout).
     * - @Header("Authorization"): Menyisipkan Bearer Token ke dalam header HTTP untuk memberi tahu server
     *   siapa pengguna yang sedang melakukan permintaan logout.
     */
    @POST("OnlineShop/auth/logout.php")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): AuthResponse<Any?>

    /**
     * Endpoint 4: Daftar Produk dengan Pagination (Halaman & Batasan Item).
     * - @GET: Mengambil data dari server.
     * - @Query: Menambahkan parameter query string di URL (contoh: ?page=1&limit=5).
     */
    @GET("OnlineShop/products/listproduct.php")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): ProductResponse

    /**
     * Endpoint 5: Pencarian Produk (Search).
     * Mengirimkan kata kunci pencarian melalui parameter query `q`.
     */
    @GET("OnlineShop/products/search.php")
    suspend fun searchProducts(
        @Query("q") query: String
    ): ProductResponse

    /**
     * Endpoint 6: Detail Produk Berdasarkan ID.
     * Mengambil informasi lengkap satu produk tertentu menggunakan parameter `id`.
     */
    @GET("OnlineShop/products/detail.php")
    suspend fun getProductDetail(
        @Query("id") productId: Int
    ): DetailProductResponse

    /**
     * Endpoint 7: Pembaruan Profil Pengguna (Update Profile).
     * Mengirimkan data perubahan username dan sandi melalui HTTP POST berformat form-urlencoded.
     */
    @FormUrlEncoded
    @POST("OnlineShop/auth/update-profile.php")
    suspend fun updateProfile(
        @Field("username") username: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): AuthResponse<UserData>

    /**
     * Endpoint 8: Menambah Produk ke Keranjang Belanja (Add to Cart).
     * Mengirimkan ID produk dan jumlah kuantitas pesanan.
     */
    @FormUrlEncoded
    @POST("OnlineShop/cart/add.php")
    suspend fun addToCart(
        @Field("product_id") productId: Int,
        @Field("quantity") quantity: Int
    ): CartResponse

    /**
     * Endpoint 9: Mengambil Daftar Pesanan Keranjang (Cart List).
     * Mengambil seluruh daftar item produk yang ada di dalam keranjang belanja pengguna yang sedang aktif.
     */
    @GET("OnlineShop/cart/listpesanan.php")
    suspend fun getCartList(): CartListResponse
}