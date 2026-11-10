package com.nandaadisaputra.onlineshop.ui.product

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nandaadisaputra.onlineshop.R
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.helper.toFile
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityAddProductBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AdminProductViewModel
import java.io.File

/**
 * AddProductActivity adalah halaman (View dalam arsitektur MVVM) khusus untuk akun Administrator
 * yang berfungsi untuk menambahkan data produk baru ke dalam sistem.
 *
 * Halaman ini bertugas untuk:
 * 1. Memilih gambar produk dari galeri perangkat menggunakan Android Photo Picker.
 * 2. Memvalidasi input teks (Nama Produk, Harga Produk, dan Deskripsi Produk) serta file gambar.
 * 3. Mengirimkan data produk baru secara Multipart ke server melalui [AdminProductViewModel].
 */
class AddProductActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL, KOMPONEN & ACTIVITY RESULT LAUNCHER
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_add_product.xml secara aman tanpa findViewById()
    private lateinit var binding: ActivityAddProductBinding

    // Inisialisasi AdminProductViewModel menggunakan delegasi 'by viewModels()' untuk mengelola logika tambah produk
    private val viewModel: AdminProductViewModel by viewModels()

    // Variabel penampung objek File gambar yang dipilih dari galeri sebelum diunggah ke server
    private var selectedImageFile: File? = null

    /**
     * ActivityResultLauncher untuk membuka Android Photo Picker (Galeri Gambar).
     * Ketika admin berhasil memilih satu gambar (uri != null), gambar akan ditampilkan pada ImageView preview
     * dan dikonversi dari Uri menjadi objek [File] sementara menggunakan extension helper `toFile()`.
     */
    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Menghapus padding placeholder dan mengatur scaleType agar foto memenuhi kotak preview dengan rapi
            binding.ivProductPreview.setPadding(0, 0, 0, 0)
            binding.ivProductPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            // Menampilkan gambar yang dipilih ke komponen ImageView (ivProductPreview)
            binding.ivProductPreview.setImageURI(uri)
            // Mengonversi Uri gambar menjadi objek File agar siap dikirim melalui Multipart Retrofit
            selectedImageFile = uri.toFile(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi-fungsi modular agar struktur kode di onCreate tetap bersih dan rapi
        setupView()
        setupListeners()
        observeViewModel()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN & EVENT HANDLING (AKSI KLIK)
    // ====================================================================

    /**
     * Fungsi untuk menyiapkan teks tampilan awal halaman dari resource strings.xml.
     */
    private fun setupView() {
        // Mengatur judul halaman ("TAMBAH PRODUCT") dan teks tombol ("SIMPAN") dari strings.xml
        binding.tvFormTitle.text = getString(R.string.tambah_product)
        binding.btnSubmit.text = getString(R.string.simpan)
    }

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada komponen-komponen UI.
     */
    private fun setupListeners() {
        // 1. Aksi ketika tombol Kembali (<) diklik -> Menutup halaman saat ini dan kembali ke Dashboard Admin
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 2. Aksi ketika kotak gambar (card_image_picker) diklik -> Membuka galeri khusus file gambar (ImageOnly)
        binding.cardImagePicker.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // 3. Aksi ketika tombol SIMPAN (btn_submit) diklik -> Memvalidasi input dan mengirim data ke ViewModel
        binding.btnSubmit.setOnClickListener {
            // Mengambil teks dari kolom input dan menghapus spasi berlebih di awal/akhir menggunakan .trim()
            val nama = binding.etNamaProduct.text.toString().trim()
            val harga = binding.etHargaProduct.text.toString().trim()
            val deskripsi = binding.etDeskripsiProduct.text.toString().trim()
            val imageFile = selectedImageFile

            // Validasi 1: Memastikan tidak ada kolom teks yang dibiarkan kosong
            if (nama.isEmpty() || harga.isEmpty() || deskripsi.isEmpty()) {
                showToast(getString(R.string.msg_empty_fields))
                return@setOnClickListener
            }

            // Validasi 2: Memastikan admin sudah memilih gambar produk dari galeri
            if (imageFile == null) {
                showToast(getString(R.string.msg_empty_image))
                return@setOnClickListener
            }

            // Jika seluruh validasi terpenuhi, panggil fungsi createProduct di AdminProductViewModel
            viewModel.createProduct(nama, harga, deskripsi, imageFile)
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil pengiriman produk baru pada LiveData di ViewModel.
     */
    private fun observeViewModel() {
        viewModel.actionResult.observe(this) { state ->
            when (state) {
                // 1. KONDISI LOADING: Saat data dan file gambar sedang diunggah ke server
                is ResultState.Loading -> {
                    // Tampilkan ProgressBar dan nonaktifkan seluruh input serta tombol agar tidak terjadi klik ganda
                    showLoading(true)
                }

                // 2. KONDISI SUKSES: Server berhasil menyimpan produk baru (HTTP 201 Created / 200 OK)
                is ResultState.Success -> {
                    showLoading(false)
                    // Ambil pesan sukses dari server, atau gunakan pesan default dari strings.xml jika kosong
                    val message = state.data.message.ifEmpty {
                        getString(R.string.msg_product_added)
                    }
                    showToast(message)
                    // Tutup halaman AddProductActivity agar kembali ke AdminDashboardActivity
                    finish()
                }

                // 3. KONDISI ERROR: Gagal mengunggah produk karena kendala jaringan atau kesalahan server
                is ResultState.Error -> {
                    // Sembunyikan ProgressBar dan aktifkan kembali seluruh input agar admin bisa mencoba lagi
                    showLoading(false)
                    showToast(state.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk mengatur visibilitas ProgressBar sekaligus mengaktifkan/menonaktifkan
     * komponen input dan tombol secara otomatis menggunakan extension helper (`setLoading`).
     *
     * @param isLoading Bernilai `true` saat proses unggah berlangsung, dan `false` saat selesai.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.setLoading(
            isLoading,
            binding.btnSubmit,
            binding.etNamaProduct,
            binding.etHargaProduct,
            binding.etDeskripsiProduct,
            binding.cardImagePicker,
            binding.btnBack
        )
    }
}