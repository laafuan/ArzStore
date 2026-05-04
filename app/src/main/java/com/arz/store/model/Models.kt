package com.arz.store.model

import androidx.compose.ui.graphics.Color

data class GameProduct(
    val id: Int,
    val name: String,
    val category: String,
    val categorySlug: String,
    val iconResId: Int,
    val gradientStart: Color,
    val gradientEnd: Color,
    val isPopular: Boolean = false,
    val isNew: Boolean = false,
    val requiresZoneId: Boolean = false,
    val iconUrl: String? = null,
)

data class TopUpPackage(
    val id: Int,
    val label: String,
    val amount: Int,
    val bonus: Int = 0,
    val price: Long,
    val isPopular: Boolean = false,
)

data class BannerItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val discountText: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color,
    val iconResId: Int,
    val imageUrl: String? = null
)

data class Transaction(
    val id: String,
    val gameId: Int,
    val gameName: String,
    val packageLabel: String,
    val price: Long,
    val date: String,
    val status: String,
    val statusColor: Color,
)

data class CategoryItem(
    val id: Int,
    val name: String,
    val slug: String,
    val iconUrl: String?
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val avatarUrl: String?
)
