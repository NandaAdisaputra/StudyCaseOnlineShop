package com.nandaadisaputra.onlineshop.data.model

data class AuthResponse<T>(
    val status: Boolean,
    val code: Int,
    val message: String,
    val data: T?
)

