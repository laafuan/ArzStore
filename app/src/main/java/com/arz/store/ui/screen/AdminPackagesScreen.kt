package com.arz.store.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arz.store.model.GameProduct
import com.arz.store.model.TopUpPackage
import com.arz.store.ui.AdminViewModel
import com.arz.store.ui.MainViewModel
import com.arz.store.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPackagesScreen(
    game: GameProduct,
    adminViewModel: AdminViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit
) {
    val packages by mainViewModel.packages.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(game.id) {
        mainViewModel.loadPackages(game.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paket: ${game.name}", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentCyan,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Paket")
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(packages) { pkg ->
                AdminPackageCard(pkg, onDelete = {
                    adminViewModel.deletePackage(pkg.id) {
                        mainViewModel.loadPackages(game.id)
                    }
                })
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showAddDialog) {
            AddPackageDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { label, amount, bonus, price, isPopular ->
                    adminViewModel.createPackage(
                        gameId = game.id,
                        label = label,
                        amount = amount,
                        bonus = bonus,
                        price = price,
                        isPopular = isPopular
                    ) {
                        showAddDialog = false
                        mainViewModel.loadPackages(game.id)
                    }
                }
            )
        }
    }
}

@Composable
fun AdminPackageCard(pkg: TopUpPackage, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Rp ${pkg.price} | Bonus: ${pkg.bonus}", color = TextMuted, fontSize = 13.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = ErrorRed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPackageDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, Long, Boolean) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var bonus by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isPopular by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Tambah Paket Top-Up", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (cth: 86 Diamonds)") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Jumlah Utama") })
                OutlinedTextField(value = bonus, onValueChange = { bonus = it }, label = { Text("Bonus") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (IDR)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPopular, onCheckedChange = { isPopular = it })
                    Text("Populer?", color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(label, amount.toIntOrNull() ?: 0, bonus.toIntOrNull() ?: 0, price.toLongOrNull() ?: 0, isPopular)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
