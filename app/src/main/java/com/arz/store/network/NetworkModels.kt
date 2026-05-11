package com.arz.store.network

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T,
    @SerializedName("pagination") val pagination: PaginationDto? = null
)

data class PaginationDto(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int
)

data class GameDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("category_slug") val categorySlug: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("banner_url") val bannerUrl: String?,
    @SerializedName("gradient_start") val gradientStart: String?,
    @SerializedName("gradient_end") val gradientEnd: String?,
    @SerializedName("accent_color") val accentColor: String?,
    @SerializedName("is_popular") val isPopular: Boolean,
    @SerializedName("is_new") val isNew: Boolean,
    @SerializedName("requires_zone_id") val requiresZoneId: Boolean
)

data class PackageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("game_id") val gameId: Int,
    @SerializedName("label") val label: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("bonus") val bonus: Int,
    @SerializedName("price") val price: Long,
    @SerializedName("is_popular") val isPopular: Boolean
)

data class BannerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("game_id") val gameId: Int?,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("discount_text") val discountText: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("gradient_start") val gradientStart: String?,
    @SerializedName("gradient_end") val gradientEnd: String?,
    @SerializedName("accent_color") val accentColor: String?,
    @SerializedName("game_name") val gameName: String?
)

data class TransactionRequest(
    @SerializedName("game_id") val gameId: Int,
    @SerializedName("package_id") val packageId: Int,
    @SerializedName("game_user_id") val gameUserId: String,
    @SerializedName("game_zone_id") val gameZoneId: String?,
    @SerializedName("payment_method") val paymentMethod: String
)

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("game_id") val gameId: Int,
    @SerializedName("package_id") val packageId: Int,
    @SerializedName("game_user_id") val gameUserId: String,
    @SerializedName("game_zone_id") val gameZoneId: String?,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("bonus") val bonus: Int,
    @SerializedName("price") val price: Long,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("game_name") val gameName: String?,
    @SerializedName("package_label") val packageLabel: String?,
    @SerializedName("game_icon_url") val gameIconUrl: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class CategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("sort_order") val sortOrder: Int,
    @SerializedName("game_count") val gameCount: Int? = null
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("is_active") val isActive: Int
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val current: String,
    @SerializedName("new_password") val new: String
)

data class TransactionStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null
)
