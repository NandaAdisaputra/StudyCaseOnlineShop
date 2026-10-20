package com.nandaadisaputra.onlineshop.data.remote

import com.nandaadisaputra.onlineshop.data.model.AuthResponse
import com.nandaadisaputra.onlineshop.data.model.UserData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("OnlineShop/auth/register.php")
    suspend fun registerUser(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("alamat") alamat: String,
        @Field("no_whatsapp") noWhatsApp: String,
        @Field("password") password: String
    ): AuthResponse<UserData>

    @FormUrlEncoded
    @POST("OnlineShop/auth/login.php")
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponse<UserData>

    // Endpoint Logout baru dengan Header Bearer Token
    @POST("OnlineShop/auth/logout.php")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): AuthResponse<Any?>
}