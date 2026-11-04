package com.nandaadisaputra.onlineshop.data.model

data class CartListResponse(
    val status: Boolean = true,
    val code: Int = 200,
    val message: String?,
    val data: CartListData?
)

data class CartListData(
    val user_id: Int?,
    val total_items: Int?,
    val grand_total: Long?,
    val items: List<CartItem>?
)

data class CartItem(
    val cart_id: Int?,
    val product_id: Int?,
    val nama_product: String?,
    val harga: Long?,
    val deskripsi: String?,
    val gambar: String?,
    val quantity: Int?,
    val subtotal: Long?,
    val created_at: String?
)