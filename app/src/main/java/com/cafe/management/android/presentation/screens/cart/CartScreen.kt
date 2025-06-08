//package com.cafe.management.android.presentation.screens.cart
//
//import androidx.compose.animation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.cafe.management.android.data.models.PaymentMethod
//import com.cafe.management.android.presentation.screens.products.CartItem
//import java.math.BigDecimal
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CartScreen(
//    onNavigateBack: () -> Unit,
//    onNavigateToTransaction: () -> Unit,
//    viewModel: CartViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//    LaunchedEffect(Unit) {
//        viewModel.loadCartItems()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Кошик") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, "Назад")
//                    }
//                },
//                actions = {
//                    if (uiState.cartItems.isNotEmpty()) {
//                        IconButton(onClick = viewModel::clearCart) {
//                            Icon(Icons.Default.Delete, "Очистити кошик")
//                        }
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            if (uiState.cartItems.isNotEmpty()) {
//                CartBottomBar(
//                    total = uiState.total,
//                    selectedPaymentMethod = uiState.selectedPaymentMethod,
//                    onPaymentMethodChanged = viewModel::selectPaymentMethod,
//                    onCheckout = {
//                        viewModel.createTransaction()
//                        onNavigateToTransaction()
//                    },
//                    isProcessing = uiState.isProcessing
//                )
//            }
//        }
//    ) { paddingValues ->
//        when {
//            uiState.cartItems.isEmpty() && !uiState.isLoading -> {
//                EmptyCartMessage(
//                    onNavigateToProducts = onNavigateBack
//                )
//            }
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    itemsIndexed(uiState.cartItems) { index, item ->
//                        CartItemCard(
//                            item = item,
//                            onRemove = { viewModel.removeFromCart(index) },
//                            onUpdateWeight = { newWeight ->
//                                viewModel.updateCartItemWeight(index, newWeight)
//                            }
//                        )
//                    }
//
//                    if (uiState.cartItems.isNotEmpty()) {
//                        item {
//                            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
//                        }
//                    }
//                }
//            }
//        }
//
//        if (uiState.error != null) {
//            LaunchedEffect(uiState.error) {
//                // Show snackbar or error dialog
//            }
//        }
//    }
//}
//
//@Composable
//fun CartItemCard(
//    item: CartItem,
//    onRemove: () -> Unit,
//    onUpdateWeight: (Int) -> Unit
//) {
//    var showEditDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(
//                        text = item.productName,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Text(
//                        text = "${item.pricePer100g}₴/100г",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    Text(
//                        text = "${item.weightGrams}г",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "${item.totalPrice.setScale(2)}₴",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    IconButton(onClick = { showEditDialog = true }) {
//                        Icon(Icons.Default.Edit, "Редагувати")
//                    }
//
//                    IconButton(onClick = onRemove) {
//                        Icon(
//                            Icons.Default.Delete,
//                            "Видалити",
//                            tint = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    if (showEditDialog) {
//        EditCartItemDialog(
//            item = item,
//            onDismiss = { showEditDialog = false },
//            onSave = { newWeight ->
//                onUpdateWeight(newWeight)
//                showEditDialog = false
//            }
//        )
//    }
//}
//
//@Composable
//fun EditCartItemDialog(
//    item: CartItem,
//    onDismiss: () -> Unit,
//    onSave: (Int) -> Unit
//) {
//    var weight by remember { mutableStateOf(item.weightGrams.toString()) }
//    var calculatedPrice by remember { mutableStateOf(item.totalPrice) }
//
//    LaunchedEffect(weight) {
//        val weightGrams = weight.toIntOrNull() ?: 0
//        calculatedPrice = if (weightGrams > 0) {
//            item.pricePer100g * BigDecimal(weightGrams) / BigDecimal(100)
//        } else {
//            BigDecimal.ZERO
//        }
//    }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Редагувати товар") },
//        text = {
//            Column {
//                Text(
//                    text = item.productName,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Medium
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                OutlinedTextField(
//                    value = weight,
//                    onValueChange = { weight = it.filter { char -> char.isDigit() } },
//                    label = { Text("Вага (грам)") },
//                    suffix = { Text("г") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                if (calculatedPrice > BigDecimal.ZERO) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Нова сума: ${calculatedPrice.setScale(2)}₴",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    weight.toIntOrNull()?.let { w ->
//                        if (w > 0) onSave(w)
//                    }
//                },
//                enabled = weight.toIntOrNull()?.let { it > 0 } == true
//            ) {
//                Text("Зберегти")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Скасувати")
//            }
//        }
//    )
//}
//
//@Composable
//fun CartBottomBar(
//    total: BigDecimal,
//    selectedPaymentMethod: PaymentMethod,
//    onPaymentMethodChanged: (PaymentMethod) -> Unit,
//    onCheckout: () -> Unit,
//    isProcessing: Boolean
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shadowElevation = 8.dp,
//        color = MaterialTheme.colorScheme.surface
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            // Payment Method Selection
//            Text(
//                text = "Спосіб оплати:",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                PaymentMethod.values().forEach { method ->
//                    FilterChip(
//                        onClick = { onPaymentMethodChanged(method) },
//                        label = {
//                            Text(
//                                when (method) {
//                                    PaymentMethod.CASH -> "Готівка"
//                                    PaymentMethod.CARD -> "Картка"
//                                }
//                            )
//                        },
//                        selected = selectedPaymentMethod == method,
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Total and Checkout
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Text(
//                        text = "Загалом:",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Text(
//                        text = "${total.setScale(2)}₴",
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                Button(
//                    onClick = onCheckout,
//                    enabled = !isProcessing && total > BigDecimal.ZERO,
//                    modifier = Modifier.height(56.dp)
//                ) {
//                    if (isProcessing) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            strokeWidth = 2.dp
//                        )
//                    } else {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(Icons.Default.Payment, "Оплата")
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Оплатити")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyCartMessage(
//    onNavigateToProducts: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Icon(
//            imageVector = Icons.Default.ShoppingCart,
//            contentDescription = "Порожній кошик",
//            modifier = Modifier.size(120.dp),
//            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text(
//            text = "Кошик порожній",
//            style = MaterialTheme.typography.headlineMedium,
//            textAlign = TextAlign.Center
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Додайте товари для створення замовлення",
//            style = MaterialTheme.typography.bodyLarge,
//            textAlign = TextAlign.Center,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(
//            onClick = onNavigateToProducts,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Icon(Icons.Default.Add, "Додати товари")
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Додати товари")
//        }
//    }
//}
