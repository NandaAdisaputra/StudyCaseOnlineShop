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
 * HomeFragment adalah halaman utama (View berbasis Fragment dalam arsitektur MVVM) yang bertanggung jawab
 * untuk menampilkan daftar produk (product list), menangani mekanisme Pagination (Infinite Scrolling),
 * serta menyediakan fitur Pencarian (Search) produk.
 */
class HomeFragment : Fragment() {

    // 1. Deklarasi ViewBinding untuk Fragment.
    // Menggunakan dua variabel (_binding dan binding) karena siklus hidup tampilan (View) Fragment
    // bisa dihancurkan lebih dulu saat pengguna berpindah tab, meskipun objek Fragment-nya masih aktif.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 2. Deklarasi HomeViewModel sebagai pengelola data (State Management)
    private lateinit var viewModel: HomeViewModel

    // 3. Deklarasi ProductAdapter untuk menghubungkan data list produk ke RecyclerView
    private lateinit var productAdapter: ProductAdapter

    // Flag penanda (boolean) apakah pengguna sedang dalam mode pencarian atau tidak
    private var isSearching = false

    /**
     * Fungsi wajib 1 di Fragment: Digunakan untuk merakit/mengubah (inflate) file layout XML
     * (`fragment_home.xml`) menjadi objek View agar dapat dirender ke layar.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Fungsi wajib 2 di Fragment: Dipanggil tepat setelah komponen View selesai dibuat.
     * Tempat terbaik untuk menginisialisasi ViewModel, Adapter, Listener RecyclerView,
     * mengatur fitur pencarian, dan memanggil API pertama kali.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menginisialisasi HomeViewModel menggunakan ViewModelProvider yang terikat pada Lifecycle Fragment ini
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Memanggil fungsi pendukung untuk menyiapkan RecyclerView, Observer LiveData, dan Fitur Search
        setupRecyclerView()
        observeViewModel()

        // Memuat data produk halaman pertama kali saat fragment dibuka (limit 5 item per halaman)
        viewModel.fetchProducts(page = 1, limit = 5)

        // Mengatur interaksi komponen pencarian (Search Bar)
        setupSearch()
    }

    /**
     * Fungsi untuk mengatur konfigurasi RecyclerView, inisialisasi Adapter, dan ScrollListener (Pagination).
     */
    private fun setupRecyclerView() {
        // Menginisialisasi ProductAdapter serta menyisipkan fungsi Lambda
        // untuk menangani aksi klik pada salah satu kartu produk
        productAdapter = ProductAdapter { product ->
            // Mengarahkan pengguna ke DetailUserActivity dengan membawa ID produk melalui Intent
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

            // Menambahkan RecyclerView.OnScrollListener untuk mendeteksi posisi guliran layar (Infinite Scroll / Pagination)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // SYARAT MELAKUKAN LOAD MORE (PAGINATION):
                    // 1. Tidak sedang dalam mode pencarian (!isSearching)
                    // 2. Tidak sedang dalam proses memuat data lain (!viewModel.isLoadingMore)
                    // 3. Halaman saat ini belum mencapai halaman terakhir (currentPage < totalPages)
                    if (!isSearching && !viewModel.isLoadingMore && viewModel.currentPage < viewModel.totalPages) {
                        // Mengecek apakah posisi item yang sedang terlihat sudah mencapai bagian paling bawah daftar
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                        ) {
                            // Hitung halaman berikutnya dan panggil fungsi fetchProducts dengan parameter isLoadMore = true
                            val nextPage = viewModel.currentPage + 1
                            viewModel.fetchProducts(page = nextPage, limit = 5, isLoadMore = true)
                        }
                    }
                }
            })
        }
    }

    /**
     * Fungsi untuk mengatur logika pencarian produk menggunakan EditText (Search Field).
     */
    private fun setupSearch() {
        // 1. Mendeteksi penggunaan tombol "Search" atau "Enter" pada keyboard virtual Android
        binding.etSearchProduct.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchProduct.text.toString().trim()
                hideKeyboard() // Sembunyikan keyboard virtual setelah tombol search ditekan

                if (query.isNotEmpty()) {
                    isSearching = true
                    viewModel.searchProducts(query) // Panggil fungsi pencarian di ViewModel
                } else {
                    isSearching = false
                    viewModel.fetchProducts(
                        page = 1,
                        limit = 5
                    ) // Muat ulang data awal jika kolom kosong
                }
                true
            } else {
                false
            }
        }

        // 2. Memantau perubahan teks secara real-time pada EditText menggunakan TextWatcher
        // Jika teks dihapus total (kosong), maka otomatis mereset pencarian dan menampilkan seluruh data kembali.
        binding.etSearchProduct.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            if (query.isEmpty()) {
                if (isSearching) {
                    isSearching = false
                }
                // Otomatis menampilkan kembali seluruh data awal ketika kolom pencarian dikosongkan oleh pengguna
                viewModel.fetchProducts(page = 1, limit = 5)
            }
        }
    }

    /**
     * Fungsi untuk mengamati (observe) perubahan state pada LiveData (`productResult`) di dalam HomeViewModel.
     * Setiap kali status jaringan berubah (Loading -> Success / Error), UI akan otomatis diperbarui secara reaktif.
     */
    private fun observeViewModel() {
        viewModel.productResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                // 1. KONDISI LOADING: Saat permintaan data ke server sedang berlangsung
                is ResultState.Loading -> {
                    // Tampilkan ProgressBar utama di layar
                    binding.progressBar.visibility = View.VISIBLE
                }

                // 2. KONDISI SUKSES: Saat server berhasil mengembalikan data produk
                is ResultState.Success -> {
                    // Sembunyikan ProgressBar saat data berhasil diterima
                    binding.progressBar.visibility = View.GONE

                    // Mengambil daftar produk dari respons API, berikan list kosong jika null
                    val products = result.data.data?.products.orEmpty()

                    // Memasukkan daftar data produk ke dalam Adapter menggunakan DiffUtil (`submitList`)
                    productAdapter.submitList(products.toList())
                }

                // 3. KONDISI ERROR: Saat terjadi kendala koneksi atau server bermasalah
                is ResultState.Error -> {
                    // Sembunyikan ProgressBar jika terjadi kesalahan
                    binding.progressBar.visibility = View.GONE

                    // Menampilkan pesan error kepada pengguna menggunakan Toast
                    requireContext().showToast(result.message)
                }
            }
        }
    }

    /**
     * Fungsi pembantu (Helper Function) untuk menutup/menyembunyikan keyboard virtual di layar perangkat.
     */
    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearchProduct.windowToken, 0)
    }

    /**
     * Fungsi onDestroyView dipanggil ketika tampilan Fragment dihancurkan (misalnya saat pengguna pindah menu tab).
     * Wajib menetapkan `_binding = null` untuk mencegah terjadinya Memory Leak pada memori RAM.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}