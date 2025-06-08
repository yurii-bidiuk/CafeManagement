package com.cafe.management.android.presentation.screens.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cafe.management.android.data.models.PaymentMethod
import com.cafe.management.android.presentation.composables.LoadingDialog
import com.cafe.management.android.presentation.screens.products.CartItem
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    cartItems: List<CartItem>,
    onNavigateBack: () -> Unit,
    onTransactionComplete: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize cart items
    LaunchedEffect(cartItems) {
        viewModel.setCartItems(cartItems)
    }

    // Handle successful transaction
    LaunchedEffect(uiState.completedTransactionId) {
        uiState.completedTransactionId?.let { transactionId ->
            onTransactionComplete(transactionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оформлення замовлення") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            TransactionBottomBar(
                totalAmount = uiState.totalAmount,
                discountAmount = uiState.discountAmount,
                finalAmount = uiState.finalAmount,
                paymentMethod = uiState.paymentMethod,
                onPaymentMethodChange = viewModel::setPaymentMethod,
                onConfirm = viewModel::createTransaction,
                isEnabled = !uiState.isLoading && uiState.cartItems.isNotEmpty()
            )
        }
    ) { paddingValues ->
        if (uiState.cartItems.isEmpty()) {
            EmptyCartState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.cartItems,
                    key = { item -> "${item.product.id}_${item.weightGrams}" }
                ) { item ->
                    CartItemCard(
                        item = item,
                        onRemove = { viewModel.removeFromCart(item) }
                    )
                }

                // Discount section
                item {
                    DiscountSection(
                        discount = uiState.discount,
                        onApplyDiscount = viewModel::applyDiscount,
                        onRemoveDiscount = viewModel::removeDiscount
                    )
                }

                // Summary
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryCard(
                        totalAmount = uiState.totalAmount,
                        discountAmount = uiState.discountAmount,
                        finalAmount = uiState.finalAmount
                    )
                }

                // Spacer for bottom bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Loading dialog
        if (uiState.isLoading) {
            LoadingDialog(message = "Оформлення транзакції...")
        }

        // Error dialog
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = viewModel::clearError,
                title = { Text("Помилка") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartItemCard(
    item: CartItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.weightGrams}г × ${formatPrice(item.product.pricePer100g)}/100г",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatPrice(item.totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Видалити",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscountSection(
    discount: Discount?,
    onApplyDiscount: (DiscountType, BigDecimal) -> Unit,
    onRemoveDiscount: () -> Unit
) {
    var showDiscountDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        if (discount == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Discount,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Додати знижку",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                IconButton(onClick = { showDiscountDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Додати",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Знижка",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = when (discount.type) {
                            DiscountType.PERCENTAGE -> "${discount.value}%"
                            DiscountType.FIXED -> formatPrice(discount.value)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                IconButton(onClick = onRemoveDiscount) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Видалити",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDiscountDialog) {
        DiscountDialog(
            onConfirm = { type, value ->
                onApplyDiscount(type, value)
                showDiscountDialog = false
            },
            onDismiss = { showDiscountDialog = false }
        )
    }
}

@Composable
private fun SummaryCard(
    totalAmount: BigDecimal,
    discountAmount: BigDecimal,
    finalAmount: BigDecimal
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сума:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatPrice(totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            if (discountAmount > BigDecimal.ZERO) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Знижка:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "-${formatPrice(discountAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "До сплати:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(finalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TransactionBottomBar(
    totalAmount: BigDecimal,
    discountAmount: BigDecimal,
    finalAmount: BigDecimal,
    paymentMethod: PaymentMethod,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    isEnabled: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Payment method selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethod.values().forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { onPaymentMethodChange(method) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (method) {
                                        PaymentMethod.CASH -> Icons.Default.Money
                                        PaymentMethod.CARD -> Icons.Default.CreditCard
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (method) {
                                        PaymentMethod.CASH -> "Готівка"
                                        PaymentMethod.CARD -> "Картка"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isEnabled
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Оформити (${formatPrice(finalAmount)})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscountDialog(
    onConfirm: (DiscountType, BigDecimal) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(DiscountType.PERCENTAGE) }
    var value by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Додати знижку") },
        text = {
            Column {
                // Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiscountType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    text = when (type) {
                                        DiscountType.PERCENTAGE -> "Відсоток %"
                                        DiscountType.FIXED -> "Сума ₴"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Value input
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '.' }) {
                            value = newValue
                            isError = false
                        }
                    },
                    label = {
                        Text(
                            when (selectedType) {
                                DiscountType.PERCENTAGE -> "Відсоток (0-100)"
                                DiscountType.FIXED -> "Сума"
                            }
                        )
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Введіть коректне значення")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val numValue = value.toBigDecimalOrNull()
                    if (numValue != null && numValue > BigDecimal.ZERO) {
                        if (selectedType == DiscountType.PERCENTAGE && numValue > BigDecimal(100)) {
                            isError = true
                        } else {
                            onConfirm(selectedType, numValue)
                        }
                    } else {
                        isError = true
                    }
                },
                enabled = value.isNotEmpty()
            ) {
                Text("Застосувати")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}

@Composable
private fun EmptyCartState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Кошик порожній",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatPrice(price: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))
    return format.format(price)
}

enum class DiscountType {
    PERCENTAGE,
    FIXED
}

data class Discount(
    val type: DiscountType,
    val value: BigDecimal
)
