package com.nandaadisaputra.onlineshop.data.model

data class CartResponse(
    val status: Boolean,
    val code: Int,
    val message: String?,
    val data: CartData?
)

data class CartData(
    val cart_id: Int?,
    val quantity: Int?,
    val product_id: Int?,
    val nama_product: String?,
    val harga: String?,
    val gambar: String?,
    val total_harga: Any?
)