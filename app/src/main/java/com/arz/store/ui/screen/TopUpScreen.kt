package com.arz.store.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arz.store.ui.MainViewModel
import com.arz.store.model.*
import com.arz.store.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun Long.toRupiah(): String {
    val format = NumberFormat.getInstance(Locale("id", "ID"))
    return "Rp ${format.format(this)}"
}

@Composable
fun TopUpScreen(
    game: GameProduct,
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(game.id) {
        viewModel.loadPackages(game.id)
    }

    val packages by viewModel.packages.collectAsState()
    var selectedPackage by remember { mutableStateOf<TopUpPackage?>(null) }
    var userId by remember { mutableStateOf("") }
    var zoneId by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Dana") }

    val paymentMethods = listOf("Dana", "GoPay", "OVO", "QRIS", "Bank Transfer")

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            // Header
            item {
                GameHeader(game = game, onBack = onBack)
            }

            // User ID Input
            item {
                Spacer(modifier = Modifier.height(16.dp))
                UserIdInputSection(
                    userId = userId,
                    onUserIdChange = { userId = it },
                    zoneId = zoneId,
                    onZoneIdChange = { zoneId = it },
                    game = game,
                )
            }

            // Packages Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih Nominal",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Package Grid
            val chunkSize = 2
            val chunkedPackages = packages.chunked(chunkSize)
            items(chunkedPackages) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { pkg ->
                        PackageCard(
                            pkg = pkg,
                            isSelected = selectedPackage?.id == pkg.id,
                            onClick = { selectedPackage = pkg },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size < chunkSize) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Payment Method
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentPurple)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Metode Pembayaran",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                PaymentMethodSection(
                    methods = paymentMethods,
                    selectedMethod = selectedPayment,
                    onMethodSelect = { selectedPayment = it },
                )
            }
        }

        // Bottom Buy Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkBg, DarkBg)
                    )
                )
                .padding(16.dp)
        ) {
            BuyButton(
                selectedPackage = selectedPackage,
                userId = userId,
                zoneId = zoneId,
                game = game,
                paymentMethod = selectedPayment,
                onBuyClick = {
                    val isIdValid = if (game.requiresZoneId) userId.isNotEmpty() && zoneId.isNotEmpty() else userId.isNotEmpty()
                    if (selectedPackage != null && isIdValid) {
                        showConfirmDialog = true
                    }
                },
            )
        }
    }

    // Confirm Dialog
    if (showConfirmDialog) {
        val fullUserId = if (game.requiresZoneId) "$userId ($zoneId)" else userId
        ConfirmOrderDialog(
            game = game,
            pkg = selectedPackage!!,
            userId = fullUserId,
            paymentMethod = selectedPayment,
            onConfirm = {
                showConfirmDialog = false
                isProcessing = true
                viewModel.createTransaction(
                    gameId = game.id,
                    packageId = selectedPackage!!.id,
                    gameUserId = userId,
                    gameZoneId = if (game.requiresZoneId) zoneId else null,
                    paymentMethod = selectedPayment
                ) { success, errorMessage ->
                    isProcessing = false
                    if (success) {
                        viewModel.loadTransactions() // Refresh history
                        when (selectedPayment) {
                            "GoPay" -> {
                                val intent = context.packageManager.getLaunchIntentForPackage("com.gojek.app")
                                if (intent != null) context.startActivity(intent)
                                showSuccessDialog = true
                            }
                            "OVO" -> {
                                val intent = context.packageManager.getLaunchIntentForPackage("ovo.id")
                                if (intent != null) context.startActivity(intent)
                                showSuccessDialog = true
                            }
                            "Dana" -> {
                                val intent = context.packageManager.getLaunchIntentForPackage("id.dana")
                                if (intent != null) context.startActivity(intent)
                                showSuccessDialog = true
                            }
                            else -> showPaymentDialog = true
                        }
                    } else {
                        showErrorDialog = errorMessage ?: "Gagal membuat transaksi"
                    }
                }
            },
            onDismiss = { showConfirmDialog = false },
        )
    }

    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(showErrorDialog!!) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) { Text("OK") }
            }
        )
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            game = game,
            pkg = selectedPackage!!,
            onDownloadClick = {
                saveImageToGallery(context, com.arz.store.R.drawable.ic_placeholder, "QR_Code_${System.currentTimeMillis()}")
            },
            onDismiss = {
                showPaymentDialog = false
                showSuccessDialog = true
            }
        )
    }


    // Success Dialog
    if (showSuccessDialog) {
        SuccessDialog(
            game = game,
            pkg = selectedPackage!!,
            onDismiss = {
                showSuccessDialog = false
                selectedPackage = null
                userId = ""
                onBack()
            },
        )
    }
}

@Composable
fun GameHeader(game: GameProduct, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(game.gradientStart, game.gradientEnd)
                )
            )
    ) {
        // Background decorations
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Game icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (game.iconUrl != null) {
                        AsyncImage(
                            model = game.iconUrl,
                            contentDescription = game.name,
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = game.iconResId),
                            contentDescription = game.name,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = game.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    Text(
                        text = game.category,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusChip("Fast Process", Icons.Filled.Bolt)
                        StatusChip("Aman", Icons.Filled.Lock)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun UserIdInputSection(
    userId: String,
    onUserIdChange: (String) -> Unit,
    zoneId: String,
    onZoneIdChange: (String) -> Unit,
    game: GameProduct,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Masukkan ID Akun",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentCyan,
                focusedContainerColor = Color(0xFF0D1526),
                unfocusedContainerColor = Color(0xFF0D1526),
            )

            if (game.requiresZoneId) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = userId,
                        onValueChange = onUserIdChange,
                        modifier = Modifier.weight(0.6f),
                        placeholder = { Text("ID Akun", color = TextMuted, fontSize = 14.sp) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = zoneId,
                        onValueChange = onZoneIdChange,
                        modifier = Modifier.weight(0.4f),
                        placeholder = { Text("Zone ID", color = TextMuted, fontSize = 14.sp) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                }
            } else {
                OutlinedTextField(
                    value = userId,
                    onValueChange = onUserIdChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Contoh: 123456789", color = TextMuted, fontSize = 14.sp)
                    },
                    colors = textFieldColors,
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )
            }

            val isValid = if (game.requiresZoneId) userId.isNotEmpty() && zoneId.isNotEmpty() else userId.isNotEmpty()

            if (isValid) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ID akan diverifikasi sebelum proses",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pastikan ID ${game.name} kamu sudah benar",
                    color = TextMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
fun PackageCard(
    pkg: TopUpPackage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentCyan else DarkCardBorder,
        label = "border",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0E2A4A) else DarkCard,
        label = "bg",
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            if (pkg.isPopular) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(WarningAmber.copy(alpha = 0.15f))
                        .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Terlaris", color = WarningAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = pkg.label,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
            )

            if (pkg.bonus > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+${pkg.bonus} Bonus",
                    color = AccentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pkg.price.toRupiah(),
                color = if (isSelected) AccentCyan else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        // Selected checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(AccentCyan),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSection(
    methods: List<String>,
    selectedMethod: String,
    onMethodSelect: (String) -> Unit,
) {
    val paymentIcons = mapOf(
        "Dana" to Icons.Filled.AccountBalanceWallet,
        "GoPay" to Icons.Filled.Payment,
        "OVO" to Icons.Filled.AccountBalance,
        "QRIS" to Icons.Filled.QrCode,
        "Bank Transfer" to Icons.Filled.CreditCard,
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        methods.forEach { method ->
            val isSelected = method == selectedMethod
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) AccentCyan else DarkCardBorder,
                label = "payment_border",
            )
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF0E2A4A) else DarkCard,
                label = "payment_bg",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { onMethodSelect(method) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    paymentIcons[method] ?: Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = if (isSelected) AccentCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = method,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(AccentCyan),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyButton(
    selectedPackage: TopUpPackage?,
    userId: String,
    zoneId: String,
    game: GameProduct,
    paymentMethod: String,
    onBuyClick: () -> Unit,
) {
    val isEnabled = selectedPackage != null && (if (game.requiresZoneId) userId.isNotEmpty() && zoneId.isNotEmpty() else userId.isNotEmpty())

    Column {
        if (selectedPackage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Total Pembayaran",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                Text(
                    text = selectedPackage.price.toRupiah(),
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isEnabled)
                            Brush.linearGradient(listOf(PrimaryBlue, AccentPurple))
                        else
                            Brush.linearGradient(listOf(DarkCard, DarkCard)),
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (!isEnabled) {
                        when {
                            selectedPackage == null -> "Pilih Nominal Terlebih Dahulu"
                            userId.isEmpty() || (game.requiresZoneId && zoneId.isEmpty()) -> "Masukkan ID Akun"
                            else -> "Beli Sekarang"
                        }
                    } else "Beli Sekarang →",
                    color = if (isEnabled) Color.White else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
fun ConfirmOrderDialog(
    game: GameProduct,
    pkg: TopUpPackage,
    userId: String,
    paymentMethod: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Konfirmasi Pesanan",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Game row with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Game", color = TextSecondary, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = game.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            game.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                        )
                    }
                }
                ConfirmRow(label = "Nominal", value = pkg.label)
                if (pkg.bonus > 0) {
                    ConfirmRow(label = "Bonus", value = "+${pkg.bonus}")
                }
                ConfirmRow(label = "ID Akun", value = userId)
                ConfirmRow(label = "Pembayaran", value = paymentMethod)
                HorizontalDivider(color = DarkCardBorder)
                ConfirmRow(
                    label = "Total",
                    value = pkg.price.toRupiah(),
                    valueColor = AccentCyan,
                    bold = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), // Using a clear blue color matching the image
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(text = "Konfirmasi", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
    )
}

@Composable
fun ConfirmRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(
            value,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun SuccessDialog(
    game: GameProduct,
    pkg: TopUpPackage,
    onDismiss: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "success_scale",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(20.dp),
        title = { },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(52.dp).scale(scale)
                )
                Text(
                    text = "Pembayaran Berhasil!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${pkg.label} untuk ${game.name} berhasil diproses",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SuccessGreen.copy(alpha = 0.1f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "Total dibayar: ${pkg.price.toRupiah()}",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Kembali ke Beranda", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
fun PaymentDialog(
    game: GameProduct,
    pkg: TopUpPackage,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Pembayaran",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Scan QR Code di bawah untuk membayar",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
                
                // QR Image
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.arz.store.R.drawable.ic_placeholder),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                // Download QR button
                OutlinedButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = BorderStroke(1.dp, AccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Filled.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download QR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                HorizontalDivider(color = DarkCardBorder)
                
                ConfirmRow(
                    label = "Total Tagihan",
                    value = pkg.price.toRupiah(),
                    valueColor = AccentCyan,
                    bold = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Saya Sudah Membayar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
    )
}

private fun saveImageToGallery(context: android.content.Context, drawableId: Int, fileName: String) {
    try {
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, drawableId)
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            
            android.widget.Toast.makeText(context, "QR Code berhasil disimpan", android.widget.Toast.LENGTH_SHORT).show()
        } ?: run {
            android.widget.Toast.makeText(context, "Gagal menyimpan QR Code", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
