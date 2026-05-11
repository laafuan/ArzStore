package com.arz.store.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arz.store.ui.MainViewModel
import com.arz.store.ui.theme.*

data class HistoryItem(
    val id: Int,
    val game: String,
    val gameIcon: ImageVector,
    val nominal: String,
    val price: String,
    val date: String,
    val status: String,
    val statusColor: Color,
)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    
    LaunchedEffect(Unit) {
        viewModel.startPollingTransactions()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPollingTransactions()
        }
    }

    val historyItems by viewModel.transactions.collectAsState()

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
                    Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), DarkBg))
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    "Riwayat Transaksi",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Text(
                    "Semua transaksi top up kamu",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(historyItems.size) { index ->
                HistoryCard(item = historyItems[index])
            }
        }
    }
}

@Composable
fun HistoryCard(item: com.arz.store.model.Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Game icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A2035)),
                contentAlignment = Alignment.Center,
            ) {
                if (item.gameIconUrl != null) {
                    AsyncImage(
                        model = item.gameIconUrl,
                        contentDescription = item.gameName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = item.iconResId),
                        contentDescription = item.gameName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.gameName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(item.packageLabel, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.date, color = TextMuted, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Rp ${item.price}",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(item.statusColor.copy(alpha = 0.12f))
                        .border(1.dp, item.statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(item.status, color = item.statusColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
