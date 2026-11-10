package com.nandaadisaputra.onlineshop.ui.detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityDetailProductAdminBinding
import com.nandaadisaputra.onlineshop.databinding.DialogConfirmDeleteBinding
import com.nandaadisaputra.onlineshop.ui.product.EditProductActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.AdminProductViewModel

/**
 * DetailProductAdminActivity adalah halaman (View dalam arsitektur MVVM) khusus untuk akun berhak akses Administrator.
 * Halaman ini bertugas menampilkan informasi detail lengkap suatu produk (Nama, Harga, Deskripsi, dan Gambar),
 * serta menyediakan tombol interaktif bagi admin untuk mengedit produk atau menghapus produk dari server.
 */
class DetailProductAdminActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL, KOMPONEN & CONSTANT EXTRA
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_detail_product_admin.xml secara aman tanpa findViewById()
    private lateinit var binding: ActivityDetailProductAdminBinding

    // Inisialisasi AdminProductViewModel menggunakan delegasi 'by viewModels()' untuk mengambil detail dan menghapus produk
    private val viewModel: AdminProductViewModel by viewModels()

    // Variabel untuk menyimpan ID produk yang sedang aktif
    private var productId: Int = -1

    // Variabel penampung data produk saat ini untuk diteruskan ke halaman Edit atau Dialog Hapus
    private var currentNama: String = ""
    private var currentHarga: String = ""
    private var currentDeskripsi: String = ""
    private var currentGambar: String = ""

    companion object {
        // Kunci Intent Extra untuk menerima ID produk dari halaman sebelumnya (AdminDashboardActivity)
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityDetailProductAdminBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Menerima ID produk dari Intent
        productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            showToast(getString(R.string.msg_invalid_product_id))
            finish()
            return
        }

        // Memanggil fungsi-fungsi modular agar struktur kode tetap bersih dan terorganisir
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Memuat ulang data detail produk setiap kali halaman kembali aktif (misal setelah selesai mengedit)
        if (productId != -1) {
            viewModel.fetchProductDetail(productId)
        }
    }

    // ====================================================================
    // 2. SETUP EVENT HANDLING & CUSTOM DIALOG KONFIRMASI HAPUS
    // ====================================================================

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada komponen UI di halaman Detail Produk Admin.
     */
    private fun setupListeners() {
        // Aksi ketika tombol kembali (<) diklik oleh admin -> Menutup halaman dan kembali ke Dashboard Admin
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Aksi ketika tombol EDIT diklik -> Berpindah ke EditProductActivity dengan membawa data produk saat ini
        binding.btnEdit.setOnClickListener {
            navigateToActivity<EditProductActivity> {
                putExtra(EditProductActivity.EXTRA_ID, productId)
                putExtra(EditProductActivity.EXTRA_NAMA, currentNama)
                putExtra(EditProductActivity.EXTRA_HARGA, currentHarga)
                putExtra(EditProductActivity.EXTRA_DESKRIPSI, currentDeskripsi)
                putExtra(EditProductActivity.EXTRA_GAMBAR, currentGambar)
            }
        }

        // Aksi ketika tombol HAPUS diklik -> Menampilkan Custom AlertDialog konfirmasi penghapusan produk
        binding.btnHapus.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    /**
     * Fungsi untuk menampilkan Custom AlertDialog konfirmasi penghapusan produk menggunakan string resource dinamis.
     */
    private fun showDeleteConfirmationDialog() {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Membuat latar belakang window dialog transparan agar sudut melengkung CardView terlihat rapi
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Mengisi pesan konfirmasi dengan menyisipkan nama produk secara dinamis (%1$s) dari strings.xml
        dialogBinding.tvDialogMessage.text =
            getString(R.string.format_konfirmasi_hapus, currentNama)

        // Jika tombol BATAL diklik, tutup dialog
        dialogBinding.btnBatal.setOnClickListener {
            alertDialog.dismiss()
        }

        // Jika tombol HAPUS diklik, tutup dialog dan panggil fungsi deleteProduct di ViewModel
        dialogBinding.btnKonfirmasiHapus.setOnClickListener {
            alertDialog.dismiss()
            viewModel.deleteProduct(productId)
        }

        alertDialog.show()
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state pada LiveData (Detail Produk & Status Hapus Produk) di ViewModel.
     */
    private fun observeViewModel() {
        // 1. Mengamati status pemuatan data detail produk dari server
        viewModel.detailResult.observe(this) { state ->
            when (state) {
                // KONDISI LOADING: Saat permintaan data detail sedang berlangsung
                is ResultState.Loading -> {
                    binding.progressBar.setLoading(
                        true,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                }

                // KONDISI SUKSES: Data detail produk berhasil diterima dari server
                is ResultState.Success -> {
                    binding.progressBar.setLoading(
                        false,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                    val product = state.data.data
                    if (product != null) {
                        currentNama = product.nama_product
                        currentHarga = product.harga
                        currentDeskripsi = product.deskripsi
                        currentGambar = product.gambar

                        // Menampilkan data ke komponen TextView
                        binding.tvNamaProduct.text = currentNama
                        // Menggunakan format harga dari strings.xml ("%1$s,-")
                        binding.tvHargaProduct.text =
                            getString(R.string.format_harga_detail, currentHarga)
                        binding.tvDeskripsiProduct.text = currentDeskripsi

                        // Memuat gambar produk menggunakan library Glide dengan placeholder khusus detail
                        Glide.with(this)
                            .load(currentGambar)
                            .placeholder(R.drawable.ic_placeholder_product_detail)
                            .error(R.drawable.ic_placeholder_product_detail)
                            .into(binding.ivProductDetail)
                    }
                }

                // KONDISI ERROR: Gagal memuat detail produk karena masalah jaringan atau server
                is ResultState.Error -> {
                    binding.progressBar.setLoading(
                        false,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                    showToast(state.message)
                }
            }
        }

        // 2. Mengamati status proses penghapusan produk dari server
        viewModel.deleteResult.observe(this) { state ->
            when (state) {
                // KONDISI LOADING: Saat permintaan hapus sedang dikirim ke server
                is ResultState.Loading -> {
                    binding.progressBar.setLoading(
                        true,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                }

                // KONDISI SUKSES: Produk berhasil dihapus dari server
                is ResultState.Success -> {
                    binding.progressBar.setLoading(
                        false,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                    val message = state.data.message.ifEmpty {
                        getString(R.string.msg_product_deleted)
                    }
                    showToast(message)
                    // Menutup halaman DetailProductAdminActivity agar kembali ke AdminDashboardActivity dan memperbarui list
                    finish()
                }

                // KONDISI ERROR: Gagal menghapus produk (misal: Produk tidak ditemukan)
                is ResultState.Error -> {
                    binding.progressBar.setLoading(
                        false,
                        binding.btnEdit,
                        binding.btnHapus,
                        binding.btnBack
                    )
                    showToast(state.message)
                }
            }
        }
    }
}