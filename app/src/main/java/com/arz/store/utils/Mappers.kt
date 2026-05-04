package com.arz.store.utils

import androidx.compose.ui.graphics.Color
import com.arz.store.model.BannerItem
import com.arz.store.model.GameProduct
import com.arz.store.model.Transaction
import com.arz.store.model.TopUpPackage
import com.arz.store.network.BannerDto
import com.arz.store.network.CategoryDto
import com.arz.store.network.GameDto
import com.arz.store.network.PackageDto
import com.arz.store.network.TransactionDto
import com.arz.store.network.UserDto
import com.arz.store.R

fun String?.toColor(default: Color): Color {
    this ?: return default
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        default
    }
}

fun String?.getDrawableId(): Int {
    if (this == null || !this.startsWith("@drawable/")) return R.drawable.ic_placeholder
    val resName = this.substringAfter("@drawable/")
    return when (resName) {
        "logo_ml" -> R.drawable.logo_ml
        "banner_ml" -> R.drawable.banner_ml
        "logo_ff" -> R.drawable.logo_ff
        "banner_ff" -> R.drawable.banner_ff
        "logo_pubg" -> R.drawable.logo_pubg
        "banner_pubg" -> R.drawable.banner_pubg
        "logo_genshin" -> R.drawable.logo_genshin
        "banner_genshin" -> R.drawable.banner_genshin
        "logo_valoran" -> R.drawable.logo_valoran
        "banner_valloran" -> R.drawable.banner_valloran
        "logo_hok" -> R.drawable.logo_hok
        "banner_hok" -> R.drawable.banner_hok
        "logo_coc" -> R.drawable.logo_coc
        "banner_coc" -> R.drawable.banner_coc
        "logo_lol" -> R.drawable.logo_lol
        "banner_lol" -> R.drawable.banner_lol
        "logo_cod" -> R.drawable.logo_cod
        "banner_codm" -> R.drawable.banner_codm
        "logo_arz" -> R.drawable.logo_arz
        else -> R.drawable.ic_placeholder
    }
}

fun GameDto.toModel(): GameProduct {
    return GameProduct(
        id = this.id,
        name = this.name,
        category = this.categoryName ?: "Umum",
        categorySlug = this.categorySlug ?: "all",
        iconResId = this.iconUrl.getDrawableId(),
        iconUrl = if (this.iconUrl?.startsWith("@drawable/") == true) null else this.iconUrl,
        gradientStart = this.gradientStart.toColor(Color(0xFF1E3A8A)),
        gradientEnd = this.gradientEnd.toColor(Color(0xFF3B82F6)),
        isPopular = this.isPopular,
        isNew = this.isNew,
        requiresZoneId = this.requiresZoneId
    )
}

fun BannerDto.toModel(): BannerItem {
    return BannerItem(
        id = this.gameId ?: this.id,
        title = this.title,
        subtitle = this.subtitle ?: "",
        discountText = this.discountText ?: "",
        gradientStart = this.gradientStart.toColor(Color(0xFF1E3A8A)),
        gradientEnd = this.gradientEnd.toColor(Color(0xFF7C3AED)),
        accentColor = this.accentColor.toColor(Color(0xFF60A5FA)),
        iconResId = this.imageUrl.getDrawableId(),
        imageUrl = if (this.imageUrl?.startsWith("@drawable/") == true) null else this.imageUrl
    )
}

fun PackageDto.toModel(): TopUpPackage {
    return TopUpPackage(
        id = this.id,
        label = this.label,
        amount = this.amount,
        bonus = this.bonus,
        price = this.price,
        isPopular = this.isPopular
    )
}

fun TransactionDto.toModel(): Transaction {
    val statusColor = when (this.status) {
        "success" -> Color(0xFF10B981) // SuccessGreen
        "failed" -> Color(0xFFEF4444) // ErrorRed
        else -> Color(0xFFF59E0B) // WarningAmber
    }
    
    val statusText = when (this.status) {
        "success" -> "Berhasil"
        "failed" -> "Gagal"
        "processing" -> "Diproses"
        else -> "Menunggu"
    }

    return Transaction(
        id = this.id,
        gameId = this.gameId,
        gameName = this.gameName ?: "Unknown Game",
        packageLabel = this.packageLabel ?: "${this.amount}",
        price = this.price,
        date = this.createdAt.take(10), // Simple substring for date
        status = statusText,
        statusColor = statusColor
    )
}

fun CategoryDto.toModel(): com.arz.store.model.CategoryItem {
    return com.arz.store.model.CategoryItem(
        id = this.id,
        name = this.name,
        slug = this.slug,
        iconUrl = this.iconUrl
    )
}

fun UserDto.toModel(): com.arz.store.model.UserProfile {
    return com.arz.store.model.UserProfile(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        role = this.role,
        avatarUrl = this.avatarUrl
    )
}
