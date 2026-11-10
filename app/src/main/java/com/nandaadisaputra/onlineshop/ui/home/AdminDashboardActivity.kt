package com.nandaadisaputra.onlineshop.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.ActivityAdminDashboardBinding
import com.nandaadisaputra.onlineshop.ui.adapter.ProductAdapter
import com.nandaadisaputra.onlineshop.ui.detail.DetailProductAdminActivity
import com.nandaadisaputra.onlineshop.ui.product.AddProductActivity
import com.nandaadisaputra.onlineshop.ui.profile.ProfileAdminActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.HomeViewModel

/**
 * AdminDashboardActivity adalah halaman utama (View dalam arsitektur MVVM) khusus untuk akun berhak akses Administrator.
 * Halaman ini bertugas menampilkan daftar seluruh produk dengan dukungan Pagination (Infinite Scroll),
 * menyediakan fitur pencarian produk secara real-time maupun via keyboard, serta menjadi pusat navigasi admin
 * menuju halaman Profil Admin, Tambah Produk, dan Detail Produk.
 */
class AdminDashboardActivity : AppCompatActivity() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================

    // Deklarasi ViewBinding untuk mengakses komponen layout activity_admin_dashboard.xml secara aman tanpa findViewById()
    private lateinit var binding: ActivityAdminDashboardBinding

    // Inisialisasi HomeViewModel menggunakan delegasi 'by viewModels()' untuk mengelola daftar produk, pagination, dan search
    private val viewModel: HomeViewModel by viewModels()

    // Adapter RecyclerView untuk menampilkan daftar item produk secara dinamis dan efisien menggunakan DiffUtil
    private lateinit var productAdapter: ProductAdapter

    // Batas maksimal jumlah item produk yang dimuat per halaman (Pagination limit)
    private val pageLimit = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengubah (inflate) file layout XML menjadi objek View di memori
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        // Menampilkan root view dari binding ke layar Activity
        setContentView(binding.root)

        // Memanggil fungsi-fungsi modular untuk struktur kode yang bersih dan terorganisir
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Memuat data produk halaman pertama (page 1) saat pertama kali halaman dibuka
        viewModel.fetchProducts(page = 1, limit = pageLimit, isLoadMore = false)
    }

    override fun onResume() {
        super.onResume()
        // Menyegarkan (refresh) daftar produk secara otomatis ketika admin kembali dari halaman Tambah / Edit / Detail Produk,
        // dengan catatan kolom pencarian sedang kosong (tidak sedang dalam mode pencarian aktif).
        if (binding.etSearch.text.isNullOrBlank()) {
            viewModel.fetchProducts(page = 1, limit = pageLimit, isLoadMore = false)
        }
    }

    // ====================================================================
    // 2. SETUP RECYCLERVIEW, PAGINATION & EVENT HANDLING
    // ====================================================================

    /**
     * Fungsi untuk menyiapkan RecyclerView, inisialisasi Adapter, dan memasang OnScrollListener
     * untuk mendukung mekanisme Infinite Scroll (Pagination otomatis saat digulir ke bawah).
     */
    private fun setupRecyclerView() {
        // Inisialisasi ProductAdapter dengan lambda higher-order untuk menangani aksi klik item produk
        productAdapter = ProductAdapter { product ->
            // Navigasi ke halaman DetailProductAdminActivity dengan mengirimkan ID produk melalui Intent Extra
            navigateToActivity<DetailProductAdminActivity> {
                putExtra(DetailProductAdminActivity.EXTRA_PRODUCT_ID, product.id)
            }
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvProducts.apply {
            this.layoutManager = layoutManager
            adapter = productAdapter

            // Menambahkan OnScrollListener untuk mendeteksi posisi guliran (scroll) guna memuat halaman berikutnya
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Periksa apakah scroll bergerak ke bawah (dy > 0) dan kotak pencarian sedang kosong
                    if (dy > 0 && binding.etSearch.text.isNullOrBlank()) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        // Jika proses load more belum berjalan dan halaman aktif masih kurang dari total halaman
                        if (!viewModel.isLoadingMore && viewModel.currentPage < viewModel.totalPages) {
                            // Jika item yang terlihat sudah mencapai bagian bawah daftar, muat halaman berikutnya
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                            ) {
                                viewModel.fetchProducts(
                                    page = viewModel.currentPage + 1,
                                    limit = pageLimit,
                                    isLoadMore = true
                                )
                            }
                        }
                    }
                }
            })
        }
    }

    /**
     * Fungsi untuk menangani seluruh aksi klik (Event Listener) pada komponen UI di Dashboard Admin.
     */
    private fun setupListeners() {
        // 1. Aksi klik pada ikon Profil Admin -> Berpindah ke halaman ProfileAdminActivity
        binding.btnProfile.setOnClickListener {
            navigateToActivity<ProfileAdminActivity>()
        }

        // 2. Aksi klik pada FloatingActionButton Tambah Produk (+) -> Berpindah ke halaman AddProductActivity
        binding.fabAddProduct.setOnClickListener {
            navigateToActivity<AddProductActivity>()
        }

        // 3. Aksi tarik ke bawah untuk menyegarkan data (Pull-to-Refresh)
        binding.swipeRefresh.setOnRefreshListener {
            binding.etSearch.text?.clear()
            viewModel.fetchProducts(page = 1, limit = pageLimit, isLoadMore = false)
        }

        // 4. Aksi pencarian produk ketika tombol 'Search / Enter' ditekan pada keyboard perangkat
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchProducts(query)
                } else {
                    viewModel.fetchProducts(page = 1, limit = pageLimit, isLoadMore = false)
                }
                true
            } else {
                false
            }
        }

        // 5. Aksi pencarian otomatis secara real-time saat teks diketik atau dihapus di dalam kolom pencarian
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    viewModel.fetchProducts(page = 1, limit = pageLimit, isLoadMore = false)
                } else if (query.length >= 2) {
                    viewModel.searchProducts(query)
                }
            }
        })
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================

    /**
     * Fungsi untuk mengamati (observe) perubahan state hasil pemuatan atau pencarian produk pada LiveData di HomeViewModel.
     */
    private fun observeViewModel() {
        viewModel.productResult.observe(this) { state ->
            when (state) {
                // 1. KONDISI LOADING: Saat permintaan data ke server sedang berlangsung
                is ResultState.Loading -> {
                    // Tampilkan ProgressBar utama jika SwipeRefreshLayout tidak sedang aktif berputar
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.setLoading(true)
                    }
                    binding.tvEmpty.visibility = View.GONE
                }

                // 2. KONDISI SUKSES: Data produk berhasil diterima dari server
                is ResultState.Success -> {
                    // Matikan indikator refresh dan loading
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.setLoading(false)

                    // Ambil daftar produk dari respons, lalu serahkan ke adapter menggunakan ListAdapter (DiffUtil)
                    val products = state.data.data?.products.orEmpty()
                    productAdapter.submitList(products)

                    // Tampilkan pesan kosong (tv_empty) jika daftar produk kosong
                    binding.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
                }

                // 3. KONDISI ERROR: Gagal memuat data produk karena kendala jaringan atau kesalahan server
                is ResultState.Error -> {
                    // Matikan indikator refresh dan loading, lalu tampilkan pesan kesalahan
                    binding.swipeRefresh.isRefreshing = false
                    binding.progressBar.setLoading(false)
                    showToast(state.message)

                    // Jika adapter belum memiliki item sama sekali, tampilkan teks kosong
                    if (productAdapter.currentList.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}