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

class DetailUserActivity : AppCompatActivity() {

    // Deklarasi View Binding dan ViewModel
    private lateinit var binding: ActivityDetailUserBinding
    private lateinit var viewModel: DetailViewModel

    companion object {
        // Kunci untuk menerima data Intent dari halaman sebelumnya (HomeFragment)
        const val EXTRA_PRODUCT_ID = "EXTRA_PRODUCT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginisialisasi View Binding
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menginisialisasi ViewModel
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        // 1. Mengambil ID produk yang dikirim melalui Intent
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)

        // Validasi apakah ID produk valid atau tidak
        if (productId != -1) {
            // Jika valid, panggil fungsi untuk mengambil data detail produk dari API
            viewModel.fetchProductDetail(productId)
        } else {
            // Jika tidak valid, tampilkan pesan dan tutup halaman
            showToast("ID produk tidak valid.")
            finish()
        }

        // Menjalankan fungsi pendukung
        setupAction()
        observeViewModel()
    }

    /**
     * Fungsi untuk mengatur aksi pada komponen UI (seperti tombol klik).
     */
    private fun setupAction() {
        // Tombol Kembali (Back Button)
        binding.ivBackButton.setOnClickListener {
            finish() // Menutup Activity dan kembali ke halaman sebelumnya
        }

        // Tombol Beli (Buy Button)
        binding.btnBuy.setOnClickListener {
            // Aksi ketika tombol beli diklik
            showToast("Produk berhasil dipilih untuk dibeli!")
        }
    }

    /**
     * Fungsi untuk mengamati (observe) perubahan data dari ViewModel.
     */
    private fun observeViewModel() {
        viewModel.detailResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    // Tampilkan ProgressBar dan sembunyikan konten saat proses memuat data
                    showLoading(true)
                }
                is ResultState.Success -> {
                    // Sembunyikan ProgressBar karena data sudah berhasil didapat
                    showLoading(false)

                    val product = result.data.data
                    if (product != null) {
                        // Menampilkan data produk ke TextView
                        binding.tvValueNamaProduct.text = product.nama_product
                        binding.tvValueHargaProduct.text = "Rp ${product.harga}"
                        binding.tvValueDeskripsiProduct.text = product.deskripsi
                        binding.tvValueCreatedAt.text = product.created_at

                        // Memuat gambar produk menggunakan library Glide
                        Glide.with(this)
                            .load(product.gambar)
                            .placeholder(android.R.drawable.ic_menu_gallery) // Gambar placeholder saat loading
                            .error(android.R.drawable.ic_menu_close_clear_cancel) // Gambar fallback jika gagal
                            .into(binding.ivDetailProduct)
                    }
                }
                is ResultState.Error -> {
                    // Sembunyikan ProgressBar jika terjadi kesalahan
                    showLoading(false)

                    // Tampilkan pesan error dari server/aplikasi
                    showToast(result.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk mengatur visibility (tampil/sembunyi) dari ProgressBar.
     * Pastikan di file XML (`activity_detail_user.xml`) terdapat ProgressBar dengan id `progressBar`.
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}