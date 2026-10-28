package com.nandaadisaputra.onlineshop.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.FragmentHomeBinding
import com.nandaadisaputra.onlineshop.ui.adapter.ProductAdapter
import com.nandaadisaputra.onlineshop.ui.detail.DetailUserActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.HomeViewModel

/**
 * HomeFragment adalah Fragment utama yang bertanggung jawab untuk menampilkan daftar produk (product list),
 * menangani mekanisme Pagination (Infinite Scrolling), serta fitur Pencarian (Search) produk.
 */
class HomeFragment : Fragment() {

    // Deklarasi View Binding untuk mengakses komponen UI dengan aman tanpa findViewById
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Deklarasi ViewModel sebagai pengelola data (State Management)
    private lateinit var viewModel: HomeViewModel

    // Deklarasi Adapter untuk RecyclerView menggunakan ListAdapter (DiffUtil)
    private lateinit var productAdapter: ProductAdapter

    // Flag penanda apakah pengguna sedang melakukan mode pencarian atau tidak
    private var isSearching = false

    /**
     * Dipanggil saat Fragment pertamakali membuat tampilan (UI layout).
     * Di sini kita menginisialisasi View Binding untuk layout fragment_home.xml.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Dipanggil setelah view selesai dibuat. Cocok untuk inisialisasi logika,
     * ViewModel, Adapter, Listener RecyclerView, dan memanggil API pertama kali.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel terikat pada lifecycle Fragment ini
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Memanggil fungsi pendukung setup UI dan Observer
        setupRecyclerView()
        observeViewModel()

        // Memuat data produk halaman pertama kali saat fragment dibuka (limit 5 item per halaman)
        viewModel.fetchProducts(page = 1, limit = 5)

        // Mengatur interaksi komponen pencarian (Search)
        setupSearch()
    }

    /**
     * Mengatur konfigurasi RecyclerView, inisialisasi Adapter, dan ScrollListener untuk Pagination.
     */
    private fun setupRecyclerView() {
        // Inisialisasi ProductAdapter dengan Lambda untuk menangani klik item produk
        productAdapter = ProductAdapter { product ->
            val intent = Intent(requireContext(), DetailUserActivity::class.java).apply {
                putExtra("EXTRA_PRODUCT_ID", product.id)
            }
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.apply {
            this.layoutManager = layoutManager
            adapter = productAdapter
            setHasFixedSize(true) // Optimalisasi performa jika ukuran item RecyclerView tetap

            // Menambahkan RecyclerView.OnScrollListener untuk mendeteksi posisi scroll (Infinite Scroll / Pagination)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Syarat melakukan Load More (Pagination):
                    // 1. Tidak sedang dalam mode pencarian (!isSearching)
                    // 2. Tidak sedang proses memuat data lain (!viewModel.isLoadingMore)
                    // 3. Halaman saat ini belum mencapai halaman terakhir (currentPage < totalPages)
                    if (!isSearching && !viewModel.isLoadingMore && viewModel.currentPage < viewModel.totalPages) {
                        // Mengecek apakah posisi item yang terlihat sudah mencapai bagian paling bawah list
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                        ) {
                            // Hitung halaman berikutnya dan panggil fungsi fetch dengan parameter isLoadMore = true
                            val nextPage = viewModel.currentPage + 1
                            viewModel.fetchProducts(page = nextPage, limit = 5, isLoadMore = true)
                        }
                    }
                }
            })
        }
    }

    /**
     * Mengatur logika pencarian produk menggunakan EditText (Search Field).
     */
    private fun setupSearch() {
        // 1. Mendeteksi penggunaan tombol "Search" atau "Enter" pada keyboard virtual Android
        binding.etSearchProduct.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchProduct.text.toString().trim()
                hideKeyboard() // Sembunyikan keyboard setelah tombol search ditekan

                if (query.isNotEmpty()) {
                    isSearching = true
                    viewModel.searchProducts(query) // Panggil fungsi pencarian di ViewModel
                } else {
                    isSearching = false
                    viewModel.fetchProducts(page = 1, limit = 5) // Muat ulang data awal jika kosong
                }
                true
            } else {
                false
            }
        }

        // 2. Memantau perubahan teks secara real-time pada EditText
        // Jika teks dihapus total (kosong), maka otomatis mereset pencarian dan menampilkan seluruh data kembali.
        binding.etSearchProduct.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            if (query.isEmpty()) {
                if (isSearching) {
                    isSearching = false
                }
                // Otomatis menampilkan kembali seluruh data awal ketika kolom pencarian dikosongkan
                viewModel.fetchProducts(page = 1, limit = 5)
            }
        }
    }

    /**
     * Mengamati (Observe) perubahan LiveData dari ViewModel untuk memperbarui UI secara reaktif.
     */
    private fun observeViewModel() {
        viewModel.productResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    // Tampilkan ProgressBar utama saat proses permintaan data ke server sedang berjalan
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    // Sembunyikan ProgressBar saat data berhasil diterima
                    binding.progressBar.visibility = View.GONE

                    // Mengambil data produk dari respons API, berikan list kosong jika null
                    val products = result.data.data?.products.orEmpty()

                    // Memasukkan data ke dalam Adapter menggunakan DiffUtil (submitList)
                    productAdapter.submitList(products.toList())
                }
                is ResultState.Error -> {
                    // Sembunyikan ProgressBar jika terjadi kesalahan
                    binding.progressBar.visibility = View.GONE

                    // Menampilkan pesan error jika pengambilan data gagal
                    requireContext().showToast(result.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk menutup/menyembunyikan keyboard virtual di layar.
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearchProduct.windowToken, 0)
    }

    /**
     * Dipanggil saat View pada Fragment dihancurkan untuk mencegah Memory Leak (kebocoran memori).
     * Nilai _binding diatur menjadi null.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}