package com.myshelf.myshelf_app.data.remote

import com.myshelf.myshelf_app.data.remote.dto.AuthResponse
import com.myshelf.myshelf_app.data.remote.dto.ItemRequest
import com.myshelf.myshelf_app.data.remote.dto.ItemResponse
import com.myshelf.myshelf_app.data.remote.dto.OutfitRequest
import com.myshelf.myshelf_app.data.remote.dto.OutfitResponse
import com.myshelf.myshelf_app.data.remote.dto.PasswordChangeRequest
import com.myshelf.myshelf_app.data.remote.dto.UserProfileRequest
import com.myshelf.myshelf_app.data.remote.dto.UserProfileResponse
import com.myshelf.myshelf_app.data.remote.dto.UserRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WardrobeApiService {

    // --- Auth ---

    @POST("auth/register")
    suspend fun register(
        @Body request: UserRegistrationRequest
    ): Response<AuthResponse>

  /**
     * Вход: сервер Spring Boot принимает email и password как query-параметры.
     */
    @POST("auth/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    // --- Items ---

    @GET("items")
    suspend fun getItems(
        @Header("Authorization") token: String
    ): Response<List<ItemResponse>>

    @GET("items/{id}")
    suspend fun getItemById(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<ItemResponse>

    @POST("items")
    suspend fun createItem(
        @Header("Authorization") token: String,
        @Body item: ItemRequest
    ): Response<ItemResponse>

    @PUT("items/{id}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: String,
        @Body item: ItemRequest
    ): Response<ItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") itemId: String
    ): Response<Unit>

    // --- Outfits ---

    @GET("outfits")
    suspend fun getOutfits(
        @Header("Authorization") token: String
    ): Response<List<OutfitResponse>>

    @GET("outfits/{id}")
    suspend fun getOutfitById(
        @Header("Authorization") token: String,
        @Path("id") outfitId: String
    ): Response<OutfitResponse>

    @POST("outfits")
    suspend fun createOutfit(
        @Header("Authorization") token: String,
        @Body outfit: OutfitRequest
    ): Response<OutfitResponse>

    @PUT("outfits/{id}")
    suspend fun updateOutfit(
        @Header("Authorization") token: String,
        @Path("id") outfitId: String,
        @Body outfit: OutfitRequest
    ): Response<OutfitResponse>

    @DELETE("outfits/{id}")
    suspend fun deleteOutfit(
        @Header("Authorization") token: String,
        @Path("id") outfitId: String
    ): Response<Unit>

    // --- User profile & password (спецификация клиента) ---

    @PUT("users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UserProfileRequest
    ): Response<UserProfileResponse>

    /**
     * На сервере реализовано как POST /api/auth/change-password.
     */
    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: PasswordChangeRequest
    ): Response<Unit>
}
