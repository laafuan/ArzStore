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
}
