package com.nandaadisaputra.onlineshop.ui.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityDetailUserBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.DetailViewModel

/**
 * DetailUserActivity adalah halaman (View dalam arsitektur MVVM) yang bertugas untuk:
 * 1. Menampilkan informasi lengkap (detail) dari satu produk yang dipilih pengguna[cite: 12].
 * 2. Menangani aksi pemesanan produk / memasukkan produk ke keranjang belanja (Add to Cart)[cite: 13].
 */
class DetailUserActivity : AppCompatActivity() {

    // 1. Deklarasi ViewBinding untuk mengakses komponen UI di activity_detail_user.xml
    // secara langsung tanpa perlu memanggil findViewById()[cite: 12].
    private lateinit var binding: ActivityDetailUserBinding

    // 2. Deklarasi DetailViewModel sebagai jembatan logika bisnis dan penyedia data untuk Activity ini[cite: 12].
    private lateinit var viewModel: DetailViewModel

    // 3. Variabel global di dalam kelas untuk menyimpan ID produk yang sedang ditampilkan.
    // Nilai awal diisi -1 sebagai penanda default (invalid ID) jika seandainya data Intent gagal diambil.
    private var currentProductId: Int = -1

    /**
     * Companion Object berfungsi seperti variabel 'static' di Java.
     * Digunakan untuk menyimpan konstanta kunci (Key) Intent agar halaman pengirim (seperti HomeFragment
     * atau CartFragment) menggunakan nama kunci yang persis sama saat mengirim ID produk[cite: 12].
     */
    companion object {
        const val EXTRA_PRODUCT_ID = "EXTRA_PRODUCT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) layout XML menjadi objek View di memori dan menampilkannya ke layar[cite: 12]
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menginisialisasi DetailViewModel menggunakan ViewModelProvider bawaan Android[cite: 12].
        // Karena DetailViewModel mewarisi AndroidViewModel, ViewModelProvider otomatis menyuntikkan Application Context.
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        // Mengambil nilai ID produk (Int) yang dikirim melalui Intent dari halaman sebelumnya[cite: 12].
        // Parameter kedua (-1) adalah nilai default jika kunci EXTRA_PRODUCT_ID tidak ditemukan di dalam Intent[cite: 12].
        currentProductId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)

        // Validasi apakah ID produk yang diterima valid (bukan -1)[cite: 12]
        if (currentProductId != -1) {
            // Jika valid, perintahkan ViewModel untuk mengambil data detail produk dari server berdasarkan ID tersebut[cite: 12]
            viewModel.fetchProductDetail(currentProductId)
        } else {
            // Jika tidak valid (tetap -1), beri tahu pengguna lewat Toast dan langsung tutup halaman ini[cite: 8, 12]
            showToast("ID produk tidak valid.")
            finish()
        }

        // Memanggil fungsi modular untuk mengatur interaksi tombol dan pengamatan data (Observer)[cite: 12]
        setupAction()
        observeViewModel()
    }

    /**
     * Fungsi untuk mengatur aksi klik (Event Listener) pada komponen tombol di layar[cite: 12].
     */
    private fun setupAction() {
        // 1. Aksi ketika tombol kembali (ivBackButton) diklik[cite: 12]
        binding.ivBackButton.setOnClickListener {
            finish() // Menutup DetailUserActivity dan kembali ke halaman sebelumnya di Activity Stack[cite: 12]
        }

        // 2. Aksi ketika tombol BELI (btnBuy) diklik untuk memesan / memasukkan produk ke keranjang[cite: 12, 13]
        binding.btnBuy.setOnClickListener {
            // Pastikan kembali ID produk valid sebelum mengirim request ke server
            if (currentProductId != -1) {
                // Menentukan jumlah barang (quantity) yang dipesan (default = 1 per klik)
                // sesuai kebutuhan parameter endpoint POST OnlineShop/cart/add.php[cite: 13]
                val quantity = 1

                // Memanggil fungsi addToCart di ViewModel untuk mengirim product_id dan quantity ke server[cite: 13]
                viewModel.addToCart(currentProductId, quantity)
            } else {
                showToast("ID produk tidak valid.")
            }
        }
    }

    /**
     * Fungsi untuk mengamati (observe) perubahan state pada LiveData yang ada di dalam DetailViewModel[cite: 12].
     * Setiap kali nilai LiveData berubah (Loading -> Success / Error), blok kode di dalam observe akan otomatis dijalankan[cite: 6, 12].
     */
    private fun observeViewModel() {

        // =====================================================================================
        // OBSERVER 1: Mengamati hasil pengambilan data Detail Produk (GET detail.php)[cite: 5, 12]
        // =====================================================================================
        viewModel.detailResult.observe(this) { result ->
            when (result) {
                // Kondisi saat aplikasi sedang meminta data ke server[cite: 6, 12]
                is ResultState.Loading -> {
                    showLoading(true) // Tampilkan indikator ProgressBar[cite: 12]
                }

                // Kondisi saat data detail produk berhasil diterima dari server[cite: 6, 12]
                is ResultState.Success -> {
                    showLoading(false) // Sembunyikan ProgressBar[cite: 12]

                    // Mengambil objek data produk dari dalam bungkus response JSON[cite: 12]
                    val product = result.data.data

                    // Pastikan objek product tidak null sebelum ditampilkan ke komponen UI[cite: 12]
                    if (product != null) {
                        // Menampilkan teks nama, harga, deskripsi, dan tanggal dibuat ke masing-masing TextView[cite: 12]
                        binding.tvValueNamaProduct.text = product.nama_product
                        binding.tvValueHargaProduct.text = "Rp ${product.harga}"
                        binding.tvValueDeskripsiProduct.text = product.deskripsi
                        binding.tvValueCreatedAt.text = product.created_at

                        // Memuat gambar produk dari URL server ke dalam ImageView (ivDetailProduct) menggunakan Glide[cite: 12]
                        Glide.with(this)
                            .load(product.gambar) // URL gambar dari API[cite: 12]
                            .placeholder(android.R.drawable.ic_menu_gallery) // Gambar sementara saat proses unduh berlangsung[cite: 12]
                            .error(android.R.drawable.ic_menu_close_clear_cancel) // Gambar pengganti jika URL rusak/gagal dimuat[cite: 12]
                            .into(binding.ivDetailProduct) // Target ImageView[cite: 12]
                    }
                }

                // Kondisi saat gagal memuat detail produk (misal: internet mati atau produk tidak ditemukan)[cite: 6, 12]
                is ResultState.Error -> {
                    showLoading(false) // Sembunyikan ProgressBar[cite: 12]
                    showToast(result.message) // Tampilkan pesan error ke layar menggunakan Toast[cite: 8, 12]
                }
            }
        }

        // =====================================================================================
        // OBSERVER 2: Mengamati hasil aksi Pesan Produk / Beli (POST cart/add.php)[cite: 13]
        // =====================================================================================
        viewModel.cartResult.observe(this) { result ->
            when (result) {
                // Kondisi saat proses pengiriman pesanan ke keranjang sedang berlangsung[cite: 6]
                is ResultState.Loading -> {
                    showLoading(true)
                    // Menonaktifkan tombol BELI sementara agar pengguna tidak menekan tombol berkali-kali (spam click)
                    binding.btnBuy.isEnabled = false
                }

                // Kondisi saat produk berhasil ditambahkan ke keranjang di database server (HTTP 201 / 200)[cite: 6, 13]
                is ResultState.Success -> {
                    showLoading(false)
                    // Mengaktifkan kembali tombol BELI
                    binding.btnBuy.isEnabled = true

                    // Menampilkan pesan sukses dari JSON server ("Produk berhasil ditambahkan ke keranjang.")[cite: 8, 13]
                    showToast(result.data.message ?: "Produk berhasil ditambahkan ke keranjang!")
                }

                // Kondisi saat gagal menambahkan produk ke keranjang[cite: 6]
                is ResultState.Error -> {
                    showLoading(false)
                    // Mengaktifkan kembali tombol BELI agar pengguna bisa mencoba lagi
                    binding.btnBuy.isEnabled = true

                    // Menampilkan pesan penyebab kegagalan kepada pengguna[cite: 8]
                    showToast(result.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu (Helper Function) untuk mengatur tampil atau sembunyinya komponen ProgressBar
     * berdasarkan parameter boolean [isLoading][cite: 12].
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility =
                View.VISIBLE // Tampilkan ProgressBar di layar[cite: 12]
        } else {
            binding.progressBar.visibility =
                View.GONE    // Sembunyikan ProgressBar dari layar[cite: 12]
        }
    }
}