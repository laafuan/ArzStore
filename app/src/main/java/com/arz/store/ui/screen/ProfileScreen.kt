package com.arz.store.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arz.store.ui.theme.*

import com.arz.store.ui.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E3A8A), AccentPurple)
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (userProfile?.avatarUrl != null) {
                        coil.compose.AsyncImage(
                            model = userProfile?.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        val initial = userProfile?.name?.firstOrNull()?.uppercase() ?: "A"
                        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 36.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(userProfile?.name ?: "Loading...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(userProfile?.phone ?: userProfile?.email ?: "Memuat data...", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(icon = Icons.Filled.AccountBalance, label = "Riwayat Transaksi", subtitle = "Lihat semua transaksi")
            ProfileMenuItem(icon = Icons.Filled.Settings, label = "Pengaturan Akun", subtitle = "Email, password, notifikasi")
            ProfileMenuItem(icon = Icons.Filled.CardGiftcard, label = "Program Referral", subtitle = "Ajak teman & dapatkan bonus")
            ProfileMenuItem(icon = Icons.Filled.Help, label = "Bantuan & FAQ", subtitle = "Pusat bantuan")
            ProfileMenuItem(icon = Icons.Filled.Description, label = "Syarat & Ketentuan", subtitle = "")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Keluar", color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = TextMuted, fontSize = 12.sp)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
