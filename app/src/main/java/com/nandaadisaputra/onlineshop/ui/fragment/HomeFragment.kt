package com.nandaadisaputra.onlineshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import com.nandaadisaputra.onlineshop.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        // Contoh: Menyapa user berdasarkan username yang tersimpan di sesi lokal
        val username = userPreference.getUsername()
        if (!username.isNullOrEmpty()) {
            binding.tvWelcome.text = "Halo, $username! Selamat Berbelanja"
        }

        // TODO: Inisialisasi RecyclerView untuk menampilkan katalog produk dari API di sini
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}