package com.arz.store.network

import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<AuthData>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): BaseResponse<AuthData>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): BaseResponse<Any>

    @GET("api/categories")
    suspend fun getCategories(): BaseResponse<List<CategoryDto>>

    @GET("api/games")
    suspend fun getGames(
        @Query("category") category: String = "all",
        @Query("popular") popular: Boolean? = null,
        @Query("limit") limit: Int = 100
    ): BaseResponse<List<GameDto>>
    
    @GET("api/games/{id}")
    suspend fun getGameDetails(@Path("id") id: Int): BaseResponse<GameDto>

    @GET("api/games/{id}/packages")
    suspend fun getPackagesForGame(@Path("id") id: Int): BaseResponse<List<PackageDto>>

    @GET("api/banners")
    suspend fun getBanners(): BaseResponse<List<BannerDto>>

    @GET("api/users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): BaseResponse<UserDto>

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): BaseResponse<UserDto>

    @Multipart
    @PUT("api/users/me/avatar")
    suspend fun updateAvatar(
        @Header("Authorization") token: String,
        @Part avatar: okhttp3.MultipartBody.Part
    ): BaseResponse<UserDto>

    @PUT("api/users/me/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): BaseResponse<Any>

    @GET("api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): BaseResponse<List<TransactionDto>>

    @POST("api/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): BaseResponse<TransactionDto>

    // --- Admin Endpoints ---

    @GET("api/transactions")
    suspend fun getAllTransactions(
        @Header("Authorization") token: String,
        @Query("all") all: Boolean = true,
        @Query("limit") limit: Int = 100
    ): BaseResponse<List<TransactionDto>>

    @PATCH("api/transactions/{id}/status")
    suspend fun updateTransactionStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: TransactionStatusRequest
    ): BaseResponse<TransactionDto>

    @Multipart
    @POST("api/games")
    suspend fun createGame(
        @Header("Authorization") token: String,
        @Part("name") name: okhttp3.RequestBody,
        @Part("slug") slug: okhttp3.RequestBody,
        @Part("category_id") categoryId: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("gradient_start") gradientStart: okhttp3.RequestBody?,
        @Part("gradient_end") gradientEnd: okhttp3.RequestBody?,
        @Part("accent_color") accentColor: okhttp3.RequestBody?,
        @Part("is_popular") isPopular: okhttp3.RequestBody,
        @Part("is_new") isNew: okhttp3.RequestBody,
        @Part("requires_zone_id") requiresZoneId: okhttp3.RequestBody,
        @Part("sort_order") sortOrder: okhttp3.RequestBody,
        @Part icon: okhttp3.MultipartBody.Part?,
        @Part banner: okhttp3.MultipartBody.Part?
    ): BaseResponse<GameDto>

    @Multipart
    @PUT("api/games/{id}")
    suspend fun updateGame(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("name") name: okhttp3.RequestBody,
        @Part("slug") slug: okhttp3.RequestBody,
        @Part("category_id") categoryId: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("gradient_start") gradientStart: okhttp3.RequestBody?,
        @Part("gradient_end") gradientEnd: okhttp3.RequestBody?,
        @Part("accent_color") accentColor: okhttp3.RequestBody?,
        @Part("is_popular") isPopular: okhttp3.RequestBody,
        @Part("is_new") isNew: okhttp3.RequestBody,
        @Part("requires_zone_id") requiresZoneId: okhttp3.RequestBody,
        @Part("is_active") isActive: okhttp3.RequestBody,
        @Part("sort_order") sortOrder: okhttp3.RequestBody,
        @Part icon: okhttp3.MultipartBody.Part?,
        @Part banner: okhttp3.MultipartBody.Part?
    ): BaseResponse<GameDto>

    @DELETE("api/games/{id}")
    suspend fun deleteGame(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): BaseResponse<Any>

    // --- Package Management ---
    @POST("api/packages")
    suspend fun createPackage(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): BaseResponse<PackageDto>

    @PUT("api/packages/{id}")
    suspend fun updatePackage(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, Any>
    ): BaseResponse<PackageDto>

    @DELETE("api/packages/{id}")
    suspend fun deletePackage(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): BaseResponse<Any>
}
