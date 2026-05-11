package com.arz.store.repository

import android.content.Context
import android.content.SharedPreferences
import com.arz.store.model.BannerItem
import com.arz.store.model.CategoryItem
import com.arz.store.model.GameProduct
import com.arz.store.model.Transaction
import com.arz.store.model.TopUpPackage
import com.arz.store.model.UserProfile
import com.arz.store.network.ApiClient
import com.arz.store.network.LoginRequest
import com.arz.store.network.RegisterRequest
import com.arz.store.network.LogoutRequest
import com.arz.store.network.TransactionRequest
import com.arz.store.utils.toModel

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object ArzRepository {
    private val api = ApiClient.apiService
    
    private const val PREFS_NAME = "arz_store_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var authToken: String?
        get() = prefs?.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs?.edit()?.putString(KEY_AUTH_TOKEN, value)?.apply()
        }

    private var refreshToken: String?
        get() = prefs?.getString(KEY_REFRESH_TOKEN, null)
        set(value) {
            prefs?.edit()?.putString(KEY_REFRESH_TOKEN, value)?.apply()
        }

    fun hasToken(): Boolean = authToken != null

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val res = api.login(request)
            if (res.success && res.data != null) {
                authToken = res.data.accessToken
                refreshToken = res.data.refreshToken
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val res = api.register(request)
            if (res.success && res.data != null) {
                authToken = res.data.accessToken
                refreshToken = res.data.refreshToken
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        val token = authToken ?: return Result.success(Unit)
        val rToken = refreshToken
        return try {
            if (rToken != null) {
                api.logout("Bearer $token", LogoutRequest(rToken))
            }
            authToken = null
            refreshToken = null
            Result.success(Unit)
        } catch (e: Exception) {
            authToken = null
            refreshToken = null
            Result.success(Unit) // Logout locally even if network fails
        }
    }

    suspend fun getGames(category: String = "all"): List<GameProduct> {
        return try {
            val res = api.getGames(category = category)
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCategories(): List<CategoryItem> {
        return try {
            val res = api.getCategories()
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getBanners(): List<BannerItem> {
        return try {
            val res = api.getBanners()
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPackagesForGame(gameId: Int): List<TopUpPackage> {
        return try {
            val res = api.getPackagesForGame(gameId)
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getTransactions(): List<Transaction> {
        val token = authToken ?: return emptyList()
        return try {
            val res = api.getTransactions("Bearer $token")
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val token = authToken ?: return null
        return try {
            val res = api.getUserProfile("Bearer $token")
            if (res.success) res.data.toModel() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateProfile(name: String, phone: String): Result<UserProfile> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val res = api.updateProfile("Bearer $token", com.arz.store.network.UpdateProfileRequest(name, phone))
            if (res.success) Result.success(res.data.toModel())
            else Result.failure(Exception(res.message ?: "Update failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvatar(file: java.io.File): Result<UserProfile> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val part = prepareFilePart("avatar", file) ?: return Result.failure(Exception("File invalid"))
            val res = api.updateAvatar("Bearer $token", part)
            if (res.success) Result.success(res.data.toModel())
            else Result.failure(Exception(res.message ?: "Avatar update failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(current: String, new: String): Result<Unit> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val res = api.changePassword("Bearer $token", com.arz.store.network.ChangePasswordRequest(current, new))
            if (res.success) Result.success(Unit)
            else Result.failure(Exception(res.message ?: "Password change failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(e: Throwable): String {
        if (e is retrofit2.HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val response = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                return response["message"] as? String ?: "Terjadi kesalahan server (${e.code()})"
            } catch (ex: Exception) {
                return "Error server: ${e.code()}"
            }
        }
        return e.message ?: "Koneksi bermasalah"
    }

    suspend fun createTransaction(
        gameId: Int,
        packageId: Int,
        gameUserId: String,
        gameZoneId: String?,
        paymentMethod: String
    ): Result<Transaction> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val req = TransactionRequest(gameId, packageId, gameUserId, gameZoneId, paymentMethod)
            val res = api.createTransaction("Bearer $token", req)
            if (res.success) {
                Result.success(res.data.toModel())
            } else {
                Result.failure(Exception(res.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(parseError(e)))
        }
    }

    // --- Admin Functions ---

    suspend fun getAllTransactions(limit: Int = 100): List<Transaction> {
        val token = authToken ?: return emptyList()
        return try {
            val res = api.getAllTransactions("Bearer $token", all = true, limit = limit)
            if (res.success) res.data.map { it.toModel() } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateTransactionStatus(id: String, status: String, notes: String? = null): Result<Transaction> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val req = com.arz.store.network.TransactionStatusRequest(status, notes)
            val res = api.updateTransactionStatus("Bearer $token", id, req)
            if (res.success) {
                Result.success(res.data.toModel())
            } else {
                Result.failure(Exception(res.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteGame(id: Int): Result<Unit> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val res = api.deleteGame("Bearer $token", id)
            if (res.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(res.message ?: "Delete failed"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Helper to create Part
    fun createPartFromString(string: String): okhttp3.RequestBody {
        return string.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun prepareFilePart(partName: String, file: java.io.File?): okhttp3.MultipartBody.Part? {
        if (file == null) return null
        val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
        return okhttp3.MultipartBody.Part.createFormData(partName, file!!.name, requestFile)
    }

    suspend fun createGame(
        name: String, slug: String, categoryId: Int,
        description: String?, gradientStart: String?, gradientEnd: String?, accentColor: String?,
        isPopular: Boolean, isNew: Boolean, requiresZoneId: Boolean, sortOrder: Int,
        iconFile: java.io.File?, bannerFile: java.io.File?
    ): Result<GameProduct> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val res = api.createGame(
                "Bearer $token",
                createPartFromString(name),
                createPartFromString(slug),
                createPartFromString(categoryId.toString()),
                description?.let { createPartFromString(it) },
                gradientStart?.let { createPartFromString(it) },
                gradientEnd?.let { createPartFromString(it) },
                accentColor?.let { createPartFromString(it) },
                createPartFromString(if (isPopular) "1" else "0"),
                createPartFromString(if (isNew) "1" else "0"),
                createPartFromString(if (requiresZoneId) "1" else "0"),
                createPartFromString(sortOrder.toString()),
                prepareFilePart("icon", iconFile),
                prepareFilePart("banner", bannerFile)
            )
            if (res.success) {
                Result.success(res.data.toModel())
            } else {
                Result.failure(Exception(res.message ?: "Failed to create game"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun createPackage(
        gameId: Int, label: String, amount: Int, bonus: Int, price: Long, isPopular: Boolean
    ): Result<TopUpPackage> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val req = mapOf(
                "game_id" to gameId,
                "label" to label,
                "amount" to amount,
                "bonus" to bonus,
                "price" to price,
                "is_popular" to isPopular
            )
            val res = api.createPackage("Bearer $token", req)
            if (res.success) Result.success(res.data.toModel())
            else Result.failure(Exception(res.message ?: "Gagal membuat package"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePackage(id: Int): Result<Unit> {
        val token = authToken ?: return Result.failure(Exception("Not logged in"))
        return try {
            val res = api.deletePackage("Bearer $token", id)
            if (res.success) Result.success(Unit)
            else Result.failure(Exception(res.message ?: "Gagal menghapus package"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
