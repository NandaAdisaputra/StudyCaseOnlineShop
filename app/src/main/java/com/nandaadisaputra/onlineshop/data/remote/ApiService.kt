package com.nandaadisaputra.onlineshop.data.remote

import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.CartListResponse
import com.nandaadisaputra.onlineshop.data.model.CartResponse
import com.nandaadisaputra.onlineshop.data.model.DetailProductResponse
import com.nandaadisaputra.onlineshop.data.model.ProductResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    /**
     * Endpoint 10: Menambah Produk Baru (Create Product - Admin).
     * - @Multipart & @Part: Digunakan untuk mengirim form-data yang berisi teks (RequestBody) dan file gambar (MultipartBody.Part).
     */
    @Multipart
    @POST("OnlineShop/products/create.php")
    suspend fun createProduct(
        @Part("nama_product") namaProduct: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part gambar: MultipartBody.Part
    ): DetailProductResponse

    /**
     * Endpoint 11: Memperbarui Data Produk (Update Product - Admin).
     * Mengirimkan form-data teks (id, nama_product, harga, deskripsi) dan file gambar opsional melalui HTTP PUT.
     */
    @Multipart
    @PUT("OnlineShop/products/update.php")
    suspend fun updateProduct(
        @Part("id") id: RequestBody,
        @Part("nama_product") namaProduct: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part gambar: MultipartBody.Part? = null
    ): DetailProductResponse

    /**
     * Endpoint 12: Menghapus Produk Berdasarkan ID (Delete Product - Admin).
     * - @HTTP(method = "DELETE", ..., hasBody = true): Digunakan agar metode HTTP DELETE di Retrofit
     *   dapat mengirimkan form-data `id` di dalam Body sekaligus parameter Query `?id=` di URL.
     */
    @Multipart
    @HTTP(method = "DELETE", path = "OnlineShop/products/delete.php", hasBody = true)
    suspend fun deleteProduct(
        @Query("id") queryId: Int,
        @Part("id") bodyId: RequestBody
    ): DetailProductResponse
}