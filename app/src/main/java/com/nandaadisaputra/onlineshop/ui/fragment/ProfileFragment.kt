package com.nandaadisaputra.onlineshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nandaadisaputra.onlineshop.data.helper.navigateToLoginOnLogout
import com.nandaadisaputra.onlineshop.data.helper.setLoading
import com.nandaadisaputra.onlineshop.data.helper.showToast
import com.nandaadisaputra.onlineshop.data.remote.ResultState
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.FragmentProfileBinding
import com.nandaadisaputra.onlineshop.ui.viewmodel.AuthViewModel

class ProfileFragment : Fragment() {

    // ====================================================================
    // 1. DEKLARASI VARIABEL & KOMPONEN
    // ====================================================================
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Menggunakan activityViewModels agar AuthViewModel tersambung dengan benar
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menginisialisasi View Binding untuk Fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreference = UserPreference(requireContext())

        setupView()
        setupListeners()
        observeLogoutResult()
    }

    // ====================================================================
    // 2. SETUP TAMPILAN & EVENT HANDLING
    // ====================================================================
    private fun setupView() {
        // Tampilkan username yang tersimpan di SharedPreferences
        val username = userPreference.getUsername()
        binding.tvUsername.text = username ?: "Pengguna"
    }

    private fun setupListeners() {
        // Tombol Logout diklik di Fragment Profil
        binding.btnLogout.setOnClickListener {
            val token = userPreference.getToken()
            if (!token.isNullOrEmpty()) {
                // Panggil API logout server dengan membawa token aktif
                authViewModel.logout(token)
            } else {
                // Jika token tidak ada, langsung bersihkan sesi lokal
                performLocalLogout()
            }
        }
    }

    // ====================================================================
    // 3. OBSERVASI LIVE DATA DARI VIEWMODEL
    // ====================================================================
    private fun observeLogoutResult() {
        authViewModel.logoutResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    requireContext().showToast(result.data.message ?: "Logout berhasil")
                    performLocalLogout()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    // Tetap lakukan logout lokal meskipun server gagal (misal 401 Unauthorized)
                    requireContext().showToast(result.message)
                    performLocalLogout()
                }
            }
        }
    }

    // ====================================================================
    // 4. FUNGSI BANTUAN (HELPER METHODS)
    // ====================================================================

    /**
     * Mengatur status interaksi tombol Logout dan ProgressBar menggunakan extension helper.
     */
    private fun showLoading(isLoading: Boolean) {
        // Mengaktifkan/menonaktifkan ProgressBar dan btnLogout secara otomatis
        binding.progressBar.setLoading(isLoading, binding.btnLogout)
    }

    /**
     * Menghapus sesi SharedPreferences dan berpindah kembali ke LoginActivity secara bersih
     */
    private fun performLocalLogout() {
        // Menghapus data SharedPreferences
        userPreference.logout()

        // Navigasi ke LoginActivity dan bersihkan stack menggunakan helper
        requireContext().navigateToLoginOnLogout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Mencegah memory leak dengan mengosongkan binding saat view dihancurkan
        _binding = null
    }
}