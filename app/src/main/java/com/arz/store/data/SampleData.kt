package com.arz.store.data

import androidx.compose.ui.graphics.Color
import com.arz.store.R
import com.arz.store.model.BannerItem
import com.arz.store.model.GameProduct
import com.arz.store.model.TopUpPackage

object SampleData {

    val banners = listOf(
        BannerItem(
            id = 1,
            title = "Mobile Legends",
            subtitle = "Top Up Diamond & Dapatkan Bonus Ekstra!",
            discountText = "BONUS 20%",
            gradientStart = Color(0xFF1E3A8A),
            gradientEnd = Color(0xFF7C3AED),
            accentColor = Color(0xFF60A5FA),
            iconResId = R.drawable.banner_ml,
        ),
        BannerItem(
            id = 2,
            title = "Free Fire",
            subtitle = "Flash Sale Diamond FF Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF064E3B),
            gradientEnd = Color(0xFF0E7490),
            accentColor = Color(0xFF34D399),
            iconResId = R.drawable.banner_ff,
        ),
        BannerItem(
            id = 3,
            title = "PUBG Mobile",
            subtitle = "Beli UC Sekarang & Raih Outfit Legendary!",
            discountText = "PROMO SPESIAL",
            gradientStart = Color(0xFF78350F),
            gradientEnd = Color(0xFFB45309),
            accentColor = Color(0xFFFBBF24),
            iconResId = R.drawable.banner_pubg,
        ),
        BannerItem(
            id = 4,
            title = "Genshin Impact",
            subtitle = "Flash Sale Primogems Genshin Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF3730A3), // Disesuaikan ke ungu gelap
            gradientEnd = Color(0xFF6366F1),
            accentColor = Color(0xFFA5B4FC),
            iconResId = R.drawable.banner_genshin,
        ),
        BannerItem(
            id = 5,
            title = "Valorant",
            subtitle = "Beli Points Sekarang & Raih Skin Eksklusif!",
            discountText = "PROMO SPESIAL",
            gradientStart = Color(0xFF991B1B), // Merah khas Valorant
            gradientEnd = Color(0xFFEF4444),
            accentColor = Color(0xFFFECACA),
            iconResId = R.drawable.banner_valloran,
        ),
        BannerItem(
            id = 6,
            title = "Honor of Kings",
            subtitle = "Flash Sale Tokens HoK Hanya Hari Ini!",
            discountText = "HEMAT 20%",
            gradientStart = Color(0xFF1E40AF),
            gradientEnd = Color(0xFF0369A1),
            accentColor = Color(0xFF7DD3FC),
            iconResId = R.drawable.banner_hok,
        ),
        BannerItem(
            id = 7,
            title = "Clash of Clans",
            subtitle = "Flash Sale Permata CoC Hanya Hari Ini!",
            discountText = "HEMAT 10%",
            gradientStart = Color(0xFF92400E),
            gradientEnd = Color(0xFFD97706),
            accentColor = Color(0xFFFCD34D),
            iconResId = R.drawable.banner_coc,
        ),
        BannerItem(
            id = 8,
            title = "League of Legends",
            subtitle = "Flash Sale RP LoL Hanya Hari Ini!",
            discountText = "HEMAT 30%",
            gradientStart = Color(0xFF1E3A8A),
            gradientEnd = Color(0xFF1D4ED8),
            accentColor = Color(0xFFBFDBFE),
            iconResId = R.drawable.banner_lol,
        ),
        BannerItem(
            id = 9,
            title = "CODM",
            subtitle = "Flash Sale CP CODM Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF1F2937),
            gradientEnd = Color(0xFF4B5563),
            accentColor = Color(0xFFD1D5DB),
            iconResId = R.drawable.banner_codm,
        ),
        BannerItem(
            id = 10,
            title = "FC 25",
            subtitle = "Flash Sale FC Points Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF065F46),
            gradientEnd = Color(0xFF059669),
            accentColor = Color(0xFF6EE7B7),
            iconResId = R.drawable.banner_fc,
        ),
        BannerItem(
            id = 11,
            title = "Magic Chess: Go Go",
            subtitle = "Flash Sale DM Points Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF5B21B6),
            gradientEnd = Color(0xFF8B5CF6),
            accentColor = Color(0xFFDDD6FE),
            iconResId = R.drawable.banner_mcgg,
        ),
        BannerItem(
            id = 12,
            title = "Roblox",
            subtitle = "Flash Sale Robux Hanya Hari Ini!",
            discountText = "HEMAT 15%",
            gradientStart = Color(0xFF374151),
            gradientEnd = Color(0xFF111827),
            accentColor = Color(0xFF9CA3AF),
            iconResId = R.drawable.banner_roblox,
        ),
    )

    val gameProducts = listOf(
        GameProduct(
            id = 1, name = "Mobile Legends", category = "MOBA",
            categorySlug = "moba",
            iconResId = R.drawable.logo_ml,
            gradientStart = Color(0xFF1E3A8A), gradientEnd = Color(0xFF3B82F6),
            isPopular = true,
            requiresZoneId = true,
        ),
        GameProduct(
            id = 2, name = "Free Fire", category = "Battle Royale",
            categorySlug = "battle-royale",
            iconResId = R.drawable.logo_ff,
            gradientStart = Color(0xFF065F46), gradientEnd = Color(0xFF10B981),
            isPopular = true,
        ),
        GameProduct(
            id = 3, name = "PUBG Mobile", category = "Battle Royale",
            categorySlug = "battle-royale",
            iconResId = R.drawable.logo_pubg,
            gradientStart = Color(0xFF78350F), gradientEnd = Color(0xFFF59E0B),
        ),
        GameProduct(
            id = 4, name = "Genshin Impact", category = "RPG",
            categorySlug = "rpg",
            iconResId = R.drawable.logo_genshin,
            gradientStart = Color(0xFF4C1D95), gradientEnd = Color(0xFF8B5CF6),
            isNew = true,
            requiresZoneId = true, // Genshin biasanya butuh Server/Zone ID
        ),
        GameProduct(
            id = 5, name = "Valorant", category = "FPS",
            categorySlug = "fps",
            iconResId = R.drawable.logo_valoran,
            gradientStart = Color(0xFF7F1D1D), gradientEnd = Color(0xFFEF4444),
        ),
        GameProduct(
            id = 6, name = "Honor of Kings", category = "MOBA",
            categorySlug = "moba",
            iconResId = R.drawable.logo_hok,
            gradientStart = Color(0xFF1E3A5F), gradientEnd = Color(0xFF0EA5E9),
            isNew = true,
        ),
        GameProduct(
            id = 7, name = "Clash of Clans", category = "Strategy",
            categorySlug = "strategy",
            iconResId = R.drawable.logo_coc,
            gradientStart = Color(0xFF3B1A08), gradientEnd = Color(0xFFD97706),
        ),
        GameProduct(
            id = 8, name = "League of Legends", category = "MOBA",
            categorySlug = "moba",
            iconResId = R.drawable.logo_lol,
            gradientStart = Color(0xFF0C1A4A), gradientEnd = Color(0xFF2563EB),
        ),
        GameProduct(
            id = 9, name = "Call of Duty", category = "FPS",
            categorySlug = "fps",
            iconResId = R.drawable.logo_cod,
            gradientStart = Color(0xFF111827), gradientEnd = Color(0xFF374151),
        ),
        GameProduct(
            id = 10, name = "FC 25", category = "SPORT",
            categorySlug = "sport",
            iconResId = R.drawable.fc_logo,
            gradientStart = Color(0xFF064E3B), gradientEnd = Color(0xFF10B981),
        ),
        GameProduct(
            id = 11, name = "Magic Chess: Go Go", category = "MOBA",
            categorySlug = "moba",
            iconResId = R.drawable.mcgg_logo,
            gradientStart = Color(0xFF4C1D95), gradientEnd = Color(0xFF7C3AED),
        ),
        GameProduct(
            id = 12, name = "Roblox", category = "PLATFORM",
            categorySlug = "platform",
            iconResId = R.drawable.logo_roblox,
            gradientStart = Color(0xFF374151), gradientEnd = Color(0xFF4B5563),
        ),
    )

    fun getPackagesForGame(gameId: Int): List<TopUpPackage> {
        return when (gameId) {
            1 -> listOf(
                TopUpPackage(1, "86 Diamonds", 86, 0, 18000),
                TopUpPackage(2, "172 Diamonds", 172, 10, 35000),
                TopUpPackage(3, "257 Diamonds", 257, 20, 52000),
                TopUpPackage(4, "344 Diamonds", 344, 30, 69000, isPopular = true),
                TopUpPackage(5, "429 Diamonds", 429, 40, 86000),
                TopUpPackage(6, "514 Diamonds", 514, 50, 103000),
                TopUpPackage(7, "706 Diamonds", 706, 70, 140000),
                TopUpPackage(8, "878 Diamonds", 878, 100, 174000),
                TopUpPackage(9, "1412 Diamonds", 1412, 150, 280000),
                TopUpPackage(10, "2195 Diamonds", 2195, 250, 434000),
                TopUpPackage(11, "3688 Diamonds", 3688, 400, 730000),
                TopUpPackage(12, "5532 Diamonds", 5532, 700, 1095000),
            )
            2 -> listOf(
                TopUpPackage(1, "50 Diamond", 50, 0, 8000),
                TopUpPackage(2, "100 Diamond", 100, 10, 15000),
                TopUpPackage(3, "210 Diamond", 210, 20, 30000, isPopular = true),
                TopUpPackage(4, "310 Diamond", 310, 30, 44000),
                TopUpPackage(5, "520 Diamond", 520, 50, 73000),
                TopUpPackage(6, "1060 Diamond", 1060, 100, 148000),
            )
            3 -> listOf(
                TopUpPackage(1, "60 UC", 60, 0, 15000),
                TopUpPackage(2, "120 UC", 120, 0, 30000),
                TopUpPackage(3, "325 UC", 325, 0, 79000, isPopular = true),
                TopUpPackage(4, "660 UC", 660, 0, 158000),
                TopUpPackage(5, "1800 UC", 1800, 0, 429000),
                TopUpPackage(6, "3850 UC", 3850, 0, 859000),
            )
            else -> listOf(
                TopUpPackage(1, "Starter Pack", 100, 0, 15000),
                TopUpPackage(2, "Basic Pack", 250, 20, 35000),
                TopUpPackage(3, "Value Pack", 500, 50, 65000, isPopular = true),
                TopUpPackage(4, "Pro Pack", 1000, 100, 125000),
                TopUpPackage(5, "Elite Pack", 2000, 200, 245000),
                TopUpPackage(6, "Ultimate Pack", 5000, 500, 599000),
            )
        }
    }
}