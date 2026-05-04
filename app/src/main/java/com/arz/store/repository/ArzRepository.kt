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
            Result.failure(e)
        }
    }
}
