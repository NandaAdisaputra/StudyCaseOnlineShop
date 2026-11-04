package com.nandaadisaputra.onlineshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.databinding.FragmentCartBinding
import com.nandaadisaputra.onlineshop.ui.adapter.CartAdapter
import com.nandaadisaputra.onlineshop.ui.detail.DetailUserActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.CartViewModel

/**
 * CartFragment adalah halaman (View berbasis Fragment dalam arsitektur MVVM) yang bertugas
 * untuk menampilkan daftar riwayat pesanan atau keranjang belanja pengguna dari server[cite: 15].
 *
 * Berbeda dengan Activity yang berdiri sendiri, Fragment adalah komponen tampilan modular
 * yang menumpang di dalam sebuah Activity (misalnya MainActivity).
 */
class CartFragment : Fragment() {

    // 1. Deklarasi ViewBinding untuk Fragment.
    // Kita menggunakan dua variabel (_binding dan binding) khusus untuk Fragment.
    // Alasan: Siklus hidup Fragment (View) berbeda dan bisa dihancurkan lebih dulu daripada objek Fragment-nya
    // saat pengguna berpindah tab, sehingga kita harus membersihkannya untuk mencegah kebocoran memori (Memory Leak).
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // 2. Deklarasi ViewModel dan Adapter yang akan digunakan di dalam Fragment ini.
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    /**
     * Fungsi wajib 1 di Fragment: Digunakan untuk merakit/mengubah (inflate) file layout XML
     * (`fragment_cart.xml`) menjadi objek View agar dapat ditampilkan ke layar.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Mengisi _binding dengan hasil inflasi layout fragment_cart.xml
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        // Mengembalikan root view dari binding
        return binding.root
    }

    /**
     * Fungsi wajib 2 di Fragment: Dipanggil tepat setelah komponen View selesai dibuat.
     * Di sinilah tempat terbaik untuk menginisialisasi ViewModel, RecyclerView, dan mulai mengamati data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menginisialisasi CartViewModel menggunakan ViewModelProvider.
        // Konteks 'this' mengarah ke Fragment ini sendiri (ViewModel terikat pada lifecycle Fragment).
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]

        // Memanggil fungsi pendukung untuk menyiapkan RecyclerView dan Observer LiveData
        setupRecyclerView()
        observeViewModel()

        // Meminta ViewModel untuk mengambil data daftar keranjang dari server saat pertama kali dibuka[cite: 15]
        viewModel.fetchCartList()
    }

    /**
     * Fungsi onResume dipanggil setiap kali Fragment kembali aktif / terlihat di layar
     * (misalnya saat pengguna kembali dari halaman DetailUserActivity).
     */
    override fun onResume() {
        super.onResume()
        // Menyegarkan (refresh) data keranjang secara otomatis agar daftar selalu sinkron
        // jika pengguna baru saja menambah atau mengubah pesanan di halaman lain.
        viewModel.fetchCartList()
    }

    /**
     * Fungsi untuk menyiapkan konfigurasi RecyclerView (LayoutManager & Adapter).
     */
    private fun setupRecyclerView() {
        // Menginisialisasi CartAdapter serta menyisipkan fungsi Lambda (Higher-Order Function)
        // yang akan dijalankan ketika salah satu item kartu produk di dalam daftar diklik.
        cartAdapter = CartAdapter { cartItem ->
            // Jika item diklik dan product_id-nya tidak null, arahkan pengguna ke DetailUserActivity[cite: 12]
            cartItem.product_id?.let { productId ->
                requireContext().navigateToActivity<DetailUserActivity> {
                    // Mengirim ID produk melalui Intent agar DetailUserActivity tahu produk mana yang harus dimuat[cite: 12]
                    putExtra(DetailUserActivity.EXTRA_PRODUCT_ID, productId)
                }
            }
        }

        // Mengatur properti RecyclerView (rv_cart)
        binding.rvCart.apply {
            layoutManager =
                LinearLayoutManager(requireContext()) // Menyusun item secara vertikal ke bawah
            adapter = cartAdapter                                 // Memasang adapter penyuplai data
            setHasFixedSize(true)                                 // Optimalisasi performa ukuran layout
        }
    }

    /**
     * Fungsi untuk mengamati (observe) perubahan state pada LiveData (`cartListResult`) di dalam CartViewModel.
     * Setiap kali status jaringan berubah (Loading -> Success / Error), UI akan otomatis memperbarui tampilannya[cite: 6].
     */
    private fun observeViewModel() {
        // Menggunakan viewLifecycleOwner agar pengamatan data otomatis berhenti saat View Fragment dihancurkan
        viewModel.cartListResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                // 1. KONDISI LOADING: Saat aplikasi sedang mengambil data dari server[cite: 6]
                is ResultState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE // Tampilkan ProgressBar utama
                    binding.tvEmptyCart.visibility =
                        View.GONE    // Sembunyikan teks keranjang kosong
                }

                // 2. KONDISI SUKSES: Saat server berhasil mengembalikan data keranjang[cite: 6, 15]
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE // Sembunyikan ProgressBar

                    // Mengambil daftar item dari objek respons (jika null, gunakan list kosong)
                    val items = result.data.data?.items ?: emptyList()

                    // Pengecekan kondisi: Apakah keranjang memiliki isi atau kosong?
                    if (items.isNotEmpty()) {
                        // Jika ada isinya: Sembunyikan teks kosong, tampilkan RecyclerView, lalu masukkan data ke Adapter
                        binding.tvEmptyCart.visibility = View.GONE
                        binding.rvCart.visibility = View.VISIBLE
                        cartAdapter.submitList(items)
                    } else {
                        // Jika kosong: Sembunyikan RecyclerView dan tampilkan teks pemberitahuan keranjang kosong
                        binding.rvCart.visibility = View.GONE
                        binding.tvEmptyCart.visibility = View.VISIBLE
                    }
                }

                // 3. KONDISI ERROR: Saat terjadi kendala koneksi atau server bermasalah[cite: 6]
                is ResultState.Error -> {
                    binding.progressBar.visibility = View.GONE // Sembunyikan ProgressBar
                    requireContext().showToast(result.message)   // Tampilkan pesan error melalui Toast[cite: 8]
                }
            }
        }
    }

    /**
     * Fungsi onDestroyView dipanggil ketika tampilan Fragment dihancurkan (misalnya saat pengguna pindah menu tab).
     * Di sinilah kita wajib menetapkan `_binding = null` untuk mencegah terjadinya Memory Leak
     * (kebocoran memori akibat referensi View yang tertinggal di dalam memori RAM).
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}