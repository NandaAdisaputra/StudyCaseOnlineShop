package com.nandaadisaputra.onlineshop.data.repository

import com.nandaadisaputra.onlineshop.data.remote.ApiConfig
import com.nandaadisaputra.onlineshop.data.remote.ApiService

class AuthRepository(
    private val apiService: ApiService = ApiConfig.provideApiService
) {
    suspend fun register(
        username: String,
        email: String,
        alamat: String,
        noWhatsApp: String,
        password: String
    ) = apiService.registerUser(username, email, alamat, noWhatsApp, password)

    suspend fun login(
        username: String,
        password: String
    ) = apiService.loginUser(username, password)

    // Fungsi Repository untuk Logout
    suspend fun logout(token: String) = apiService.logoutUser("Bearer $token")
}