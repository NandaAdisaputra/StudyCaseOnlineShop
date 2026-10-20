package com.nandaadisaputra.onlineshop.data.model

data class UserData(
    val id: Int,
    val username: String,
    val email: String?,
    val alamat: String?,
    val no_whatsapp: String?,
    val role: String,
    val token: String? // Token hanya ada saat Login, kosong saat Register
)