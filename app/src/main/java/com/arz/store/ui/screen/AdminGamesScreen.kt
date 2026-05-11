package com.arz.store.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arz.store.model.GameProduct
import com.arz.store.ui.AdminViewModel
import com.arz.store.ui.MainViewModel
import com.arz.store.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGamesScreen(
    adminViewModel: AdminViewModel,
    mainViewModel: MainViewModel,
    onNavigateToPackages: (GameProduct) -> Unit,
    onBack: () -> Unit
) {
    val games by mainViewModel.games.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.fetchGames()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Game", color = TextPrimary) },
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
                Icon(Icons.Filled.Add, contentDescription = "Tambah Game")
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
            items(games) { game ->
                AdminGameCard(
                    game = game,
                    onDelete = {
                        adminViewModel.deleteGame(game.id) {
                            mainViewModel.fetchGames()
                        }
                    },
                    onClick = { onNavigateToPackages(game) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showAddDialog) {
            AddGameDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, slug, catId, desc, iconUri, bannerUri ->
                    val iconFile = iconUri?.let { uriToFile(context, it) }
                    val bannerFile = bannerUri?.let { uriToFile(context, it) }

                    adminViewModel.createGame(
                        name = name, slug = slug, categoryId = catId,
                        description = desc, gradientStart = null, gradientEnd = null, accentColor = null,
                        isPopular = false, isNew = true, requiresZoneId = false, sortOrder = 0,
                        iconFile = iconFile, bannerFile = bannerFile
                    ) {
                        showAddDialog = false
                        mainViewModel.fetchGames()
                    }
                }
            )
        }
    }
}

@Composable
fun AdminGameCard(game: GameProduct, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                if (game.iconUrl != null) {
                    coil.compose.AsyncImage(
                        model = game.iconUrl,
                        contentDescription = game.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(game.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(game.category, color = TextMuted, fontSize = 13.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = ErrorRed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, Uri?, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    
    var iconUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }

    val iconLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> iconUri = uri }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> bannerUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Tambah Game Baru", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Game") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = { Text("Slug (unik, cth: mobile-legends)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoryId,
                    onValueChange = { categoryId = it },
                    label = { Text("ID Kategori") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Icon Game", color = TextPrimary, fontSize = 14.sp)
                Button(onClick = { iconLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (iconUri != null) "Icon Dipilih" else "Pilih Icon")
                }

                Text("Banner Game", color = TextPrimary, fontSize = 14.sp)
                Button(onClick = { bannerLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (bannerUri != null) "Banner Dipilih" else "Pilih Banner")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catId = categoryId.toIntOrNull() ?: 1
                    onAdd(name, slug, catId, description, iconUri, bannerUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        }
    )
}
