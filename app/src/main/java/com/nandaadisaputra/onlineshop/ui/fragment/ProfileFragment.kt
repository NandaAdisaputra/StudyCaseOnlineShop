package com.nandaadisaputra.onlineshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nandaadisaputra.onlineshop.data.helper.navigateToActivity
import com.nandaadisaputra.onlineshop.data.helper.navigateToLoginOnLogout
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.FragmentProfileBinding
import com.nandaadisaputra.onlineshop.ui.profile.EditProfileActivity
import com.nandaadisaputra.onlineshop.ui.viewmodel.ProfileViewModel
import com.nandaadisaputra.onlineshop.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

/**
 * ProfileFragment adalah halaman (View berbasis Fragment dalam arsitektur MVVM) yang bertugas
 * untuk menampilkan informasi profil pengguna (seperti nama/username yang sedang aktif),
 * menyediakan akses menuju halaman Edit Profile, serta menangani proses keluar akun (Logout)[cite: 1, 2, 10].
 */
class ProfileFragment : Fragment() {

    // 1. Deklarasi ViewBinding untuk Fragment.
    // Menggunakan dua variabel (_binding dan binding) karena siklus hidup tampilan (View) Fragment
    // bisa dihancurkan lebih dulu saat pengguna berpindah tab, meskipun objek Fragment-nya masih ada.
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 2. Deklarasi UserPreference untuk membaca sesi login dan token pengguna dari penyimpanan lokal[cite: 10].
    private lateinit var userPreference: UserPreference

    // 3. Inisialisasi ProfileViewModel menggunakan delegasi 'by viewModels' dan ProfileViewModelFactory.
    // Pemanggilan ViewModel ini dibagikan bersama EditProfileActivity sehingga state logout terkelola dengan baik[cite: 2].
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory.getInstance(requireContext())
    }

    /**
     * Fungsi wajib 1 di Fragment: Mengubah (inflate) file layout XML (`fragment_profile.xml`)
     * menjadi objek View agar dapat dirender ke layar.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Fungsi wajib 2 di Fragment: Dipanggil tepat setelah komponen View selesai dibuat.
     * Tempat terbaik untuk menginisialisasi sesi lokal, memuat data awal, dan mengatur observer.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Menginisialisasi objek UserPreference dengan Context Fragment
        userPreference = UserPreference(requireContext())

        loadUserData()          // Menampilkan nama pengguna ke layar
        setupListeners()        // Mengatur aksi klik tombol Edit dan Logout
        observeLogoutState()    // Mengamati status proses logout dari ViewModel
    }

    /**
     * Fungsi onResume dipanggil setiap kali Fragment kembali aktif / terlihat di layar
     * (misalnya saat pengguna selesai mengubah profil di EditProfileActivity lalu menekan tombol kembali).
     */
    override fun onResume() {
        super.onResume()
        // Memperbarui tampilan nama secara otomatis setiap kali halaman profil kembali dibuka[cite: 1]
        loadUserData()
    }

    /**
     * Fungsi untuk membaca data sesi lokal dan menampilkannya ke TextView.
     */
    private fun loadUserData() {
        // Mengambil username dari SharedPreferences. Jika bernilai null, berikan teks cadangan "User"[cite: 10].
        val currentUsername = userPreference.getUsername() ?: "User"
        // Menampilkan nama ke TextView (tvNameValue)
        binding.tvNameValue.text = currentUsername
    }

    /**
     * Fungsi untuk menangani aksi klik (Event Listener) pada tombol-tombol di halaman profil.
     */
    private fun setupListeners() {
        // Menggunakan scope function 'apply' agar lebih rapi tanpa menulis 'binding.' berulang kali
        binding.apply {
            // 1. Aksi ketika tombol EDIT diklik: Berpindah ke EditProfileActivity menggunakan Extension Function[cite: 1, 8]
            btnEdit.setOnClickListener {
                requireContext().navigateToActivity<EditProfileActivity>()
            }

            // 2. Aksi ketika tombol LOGOUT diklik[cite: 1]
            btnLogout.setOnClickListener {
                // Mengambil token autentikasi yang tersimpan di SharedPreferences
                val token = userPreference.getToken()
                if (!token.isNullOrEmpty()) {
                    // Jika token ada, panggil fungsi logout di ViewModel untuk menghapus sesi di server
                    viewModel.logout(token)
                } else {
                    // Jika token kosong, langsung bersihkan sesi lokal saja
                    performLocalLogout()
                }
            }

            // 3. Aksi tombol kembali (ivBack)
            ivBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    /**
     * Fungsi untuk mengamati (observe) perubahan state logout dari ProfileViewModel menggunakan StateFlow.
     */
    private fun observeLogoutState() {
        // Menjalankan coroutine yang terikat dengan siklus hidup view Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle memastikan pengumpulan data hanya aktif saat Fragment berada di status STARTED
            // (terlihat di layar) dan otomatis berhenti saat di latar belakang (efisien & bebas memory leak).
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutState.collect { state ->
                    when (state) {
                        // 1. KONDISI LOADING: Saat proses logout sedang dikirim ke server
                        is ResultState.Loading -> {
                            // Menampilkan ProgressBar dan menonaktifkan tombol Edit dan Logout sementara
                            binding.progressBar.setLoading(true, binding.btnEdit, binding.btnLogout)
                        }

                        // 2. KONDISI SUKSES: Server berhasil memproses logout
                        is ResultState.Success -> {
                            binding.progressBar.setLoading(
                                false,
                                binding.btnEdit,
                                binding.btnLogout
                            )
                            // Menampilkan pesan sukses dari server menggunakan Toast
                            requireContext().showToast(state.data.message ?: "Berhasil Logout")
                            // Membersihkan data sesi lokal dan mengarahkan pengguna kembali ke halaman Login
                            performLocalLogout()
                        }

                        // 3. KONDISI ERROR: Gagal terhubung ke server atau token kadaluarsa (Unauthorized)
                        is ResultState.Error -> {
                            binding.progressBar.setLoading(
                                false,
                                binding.btnEdit,
                                binding.btnLogout
                            )
                            requireContext().showToast(state.message)
                            // Tetap bersihkan sesi lokal dan arahkan ke Login demi keamanan akun
                            performLocalLogout()
                        }

                        // 4. KONDISI AWAL (IDLE): Belum ada aksi logout yang dipicu
                        null -> Unit
                    }
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk membersihkan data sesi lokal dan membawa pengguna kembali ke halaman Login.
     */
    private fun performLocalLogout() {
        // Menghapus seluruh data SharedPreferences (token, username, role, dll.)[cite: 10]
        userPreference.logout()
        // Mengarahkan ke LoginActivity dan menghapus seluruh tumpukan halaman sebelumnya (Activity Stack)[cite: 8]
        requireContext().navigateToLoginOnLogout()
    }

    /**
     * Fungsi onDestroyView dipanggil saat tampilan Fragment dihancurkan (misalnya saat berpindah menu tab).
     * Wajib menetapkan `_binding = null` untuk mencegah terjadinya Memory Leak pada memori RAM.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}