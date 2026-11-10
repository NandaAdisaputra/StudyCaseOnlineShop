package com.nandaadisaputra.onlineshop.ui.product

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.helper.toFile
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityEditProductBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AdminProductViewModel
import java.io.File

/**
 * EditProductActivity adalah halaman (View dalam arsitektur MVVM) khusus untuk akun berhak akses Administrator.
 * Halaman ini bertugas menampilkan data awal produk yang ingin diubah, menyediakan opsi bagi admin
 * untuk memilih foto baru dari galeri, memperbarui informasi produk (Nama, Harga, dan Deskripsi),
 * serta mengirimkan perubahan data ke server melalui [AdminProductViewModel].
 */
class EditProductActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL, KOMPONEN & CONSTANT EXTRA
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_edit_product.xml secara aman tanpa findViewById()
    private lateinit var binding: ActivityEditProductBinding

    // Inisialisasi AdminProductViewModel menggunakan delegasi 'by viewModels()' untuk mengelola logika pembaruan produk
    private val viewModel: AdminProductViewModel by viewModels()

    // Variabel penampung objek File gambar baru yang dipilih dari galeri (bernilai null jika admin tidak mengganti gambar)
    private var selectedImageFile: File? = null

    // Variabel penampung ID unik produk yang sedang diedit
    private var productId: Int = -1

    companion object {
        // Kunci Intent Extra untuk menerima data produk dari halaman sebelumnya (DetailProductAdminActivity)
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_HARGA = "extra_harga"
        const val EXTRA_DESKRIPSI = "extra_deskripsi"
        const val EXTRA_GAMBAR = "extra_gambar"
    }

    /**
     * ActivityResultLauncher untuk membuka pemilih media bawaan Android (Photo Picker) khusus gambar.
     * Ketika admin memilih gambar baru (uri != null), gambar akan ditampilkan pada ImageView preview
     * dan dikonversi dari Uri menjadi objek File menggunakan extension helper `toFile()`.
     */
    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Menghapus padding placeholder dan mengatur scaleType agar foto baru memenuhi bingkai kotak secara rapi
            binding.ivEditProductPreview.setPadding(0, 0, 0, 0)
            binding.ivEditProductPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            // Menampilkan pratinjau gambar baru ke komponen ImageView
            binding.ivEditProductPreview.setImageURI(uri)
            // Mengonversi Uri gambar baru menjadi objek File di memori cache
            selectedImageFile = uri.toFile(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi-fungsi modular agar struktur kode tetap bersih, terstruktur, dan mudah dibaca
        setupView()
        populateInitialData()
        setupListeners()
        observeViewModel()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN, DATA AWAL & EVENT HANDLING
    // ====================================================================

    /**
     * Fungsi untuk menyiapkan teks tampilan awal pada komponen formulir edit produk.
     */
    private fun setupView() {
        // Mengatur teks judul halaman ("EDIT PRODUCT") dan teks tombol ("UPDATE") dari strings.xml
        binding.tvEditFormTitle.text = getString(R.string.edit)
        binding.btnEditSubmit.text = getString(R.string.update_product)
    }

    /**
     * Fungsi untuk menerima data produk yang dikirim melalui Intent Extra dan memasukkannya ke kolom input & gambar.
     */
    private fun populateInitialData() {
        // Menerima data dari halaman sebelumnya
        productId = intent.getIntExtra(EXTRA_ID, -1)
        val nama = intent.getStringExtra(EXTRA_NAMA).orEmpty()
        val harga = intent.getStringExtra(EXTRA_HARGA).orEmpty()
        val deskripsi = intent.getStringExtra(EXTRA_DESKRIPSI).orEmpty()
        val gambarUrl = intent.getStringExtra(EXTRA_GAMBAR).orEmpty()

        // Menampilkan data teks produk ke masing-masing EditText
        binding.edtEditNamaProduct.setText(nama)
        binding.edtEditHargaProduct.setText(harga)
        binding.edtDeskripsiProduct.setText(deskripsi)

        // Jika URL gambar produk tersedia, muat gambar ke ImageView menggunakan Glide
        if (gambarUrl.isNotEmpty()) {
            binding.ivEditProductPreview.setPadding(0, 0, 0, 0)
            Glide.with(this)
                .load(gambarUrl)
                .placeholder(R.drawable.ic_placeholder_product)
                .error(R.drawable.ic_placeholder_product)
                .into(binding.ivEditProductPreview)
        }
    }

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada komponen UI di halaman Edit Produk.
     */
    private fun setupListeners() {
        // Aksi ketika tombol kembali (<) diklik oleh admin -> Menutup halaman dan kembali ke detail produk
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Aksi ketika kotak pratinjau gambar diklik -> Membuka galeri foto perangkat
        binding.cardImagePicker.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Aksi ketika tombol UPDATE (btn_edit_submit) diklik oleh admin
        binding.btnEditSubmit.setOnClickListener {
            // Mengambil teks terbaru dari kolom input dan menghapus spasi kosong di awal/akhir
            val nama = binding.edtEditNamaProduct.text.toString().trim()
            val harga = binding.edtEditHargaProduct.text.toString().trim()
            val deskripsi = binding.edtDeskripsiProduct.text.toString().trim()

            // Validasi: Memastikan ID produk valid dan tidak ada kolom input teks yang kosong
            if (productId == -1 || nama.isEmpty() || harga.isEmpty() || deskripsi.isEmpty()) {
                showToast(getString(R.string.msg_invalid_edit_data))
                return@setOnClickListener
            }

            // Memanggil fungsi updateProduct di AdminProductViewModel (mengirimkan selectedImageFile yang bisa bernilai null jika gambar tidak diganti)
            viewModel.updateProduct(productId, nama, harga, deskripsi, selectedImageFile)
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil pembaruan produk pada LiveData di AdminProductViewModel.
     */
    private fun observeViewModel() {
        viewModel.actionResult.observe(this) { state ->
            when (state) {
                // 1. KONDISI LOADING: Saat data perubahan produk sedang dikirim ke server
                is ResultState.Loading -> {
                    showLoading(true) // Tampilkan ProgressBar dan nonaktifkan seluruh input & tombol
                }

                // 2. KONDISI SUKSES: Server berhasil memperbarui data produk (HTTP 200 OK)
                is ResultState.Success -> {
                    showLoading(false)
                    // Ambil pesan sukses dari server atau gunakan pesan default dari strings.xml
                    val message = state.data.message.ifEmpty {
                        getString(R.string.msg_product_updated)
                    }
                    showToast(message)
                    // Menutup halaman EditProductActivity agar kembali ke halaman sebelumnya
                    finish()
                }

                // 3. KONDISI ERROR: Gagal terhubung ke server atau terjadi kesalahan saat memperbarui data
                is ResultState.Error -> {
                    showLoading(false) // Sembunyikan ProgressBar dan aktifkan kembali komponen input
                    showToast(state.message) // Tampilkan pesan kesalahan ke layar
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar dan mengaktifkan/menonaktifkan komponen input
     * secara otomatis menggunakan extension helper (`setLoading`).
     *
     * @param isLoading Bernilai `true` saat proses pengiriman berlangsung, dan `false` saat selesai.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(
            isLoading,
            binding.btnEditSubmit,
            binding.edtEditNamaProduct,
            binding.edtEditHargaProduct,
            binding.edtDeskripsiProduct,
            binding.cardImagePicker,
            binding.btnBack
        )
    }
}