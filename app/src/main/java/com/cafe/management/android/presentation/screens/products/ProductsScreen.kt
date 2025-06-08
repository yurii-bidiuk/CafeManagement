package com.cafe.management.android.presentation.screens.products

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cafe.management.android.data.models.Category
import com.cafe.management.android.data.models.Product
import com.cafe.management.android.presentation.composables.LoadingDialog
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onNavigateToTransaction: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItemsCount by viewModel.cartItemsCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вибір продуктів") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (cartItemsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge { Text(cartItemsCount.toString()) }
                            }
                        ) {
                            IconButton(onClick = onNavigateToTransaction) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Кошик"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (cartItemsCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToTransaction,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Оформити ($cartItemsCount)")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Categories
            if (uiState.categories.isNotEmpty()) {
                CategoriesRow(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelect = viewModel::onCategorySelect,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Products
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error
                    if (errorMessage != null) {
                        ErrorMessage(
                            message = errorMessage,
                            onRetry = viewModel::loadData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                uiState.filteredProducts.isEmpty() -> {
                    EmptyState(
                        message = if (uiState.searchQuery.isNotEmpty()) {
                            "Товарів не знайдено"
                        } else {
                            "В цій категорії поки немає товарів"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    ProductsList(
                        products = uiState.filteredProducts,
                        onProductClick = viewModel::onProductSelect,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Weight input dialog
        uiState.selectedProduct?.let { product ->
            WeightInputDialog(
                product = product,
                onConfirm = { weight ->
                    viewModel.addToCart(product, weight)
                },
                onDismiss = viewModel::clearSelectedProduct
            )
        }

        // Loading dialog
        if (uiState.isCalculatingPrice) {
            LoadingDialog(message = "Розрахунок ціни...")
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Пошук товарів...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Очистити")
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("Всі") },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Done, contentDescription = null) }
                } else null
            )
        }

        items(categories) { category ->
            FilterChip(
                selected = selectedCategory?.id == category.id,
                onClick = { onCategorySelect(category) },
                label = { Text(category.name) },
                leadingIcon = if (selectedCategory?.id == category.id) {
                    { Icon(Icons.Default.Done, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isCurrentlyAvailable) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (product.isSeasonal) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Сезонний",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatPrice(product.pricePer100g),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "за 100г",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!product.isCurrentlyAvailable) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = "Тимчасово недоступний",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightInputDialog(
    product: Product,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(product.name)
                Text(
                    text = "${formatPrice(product.pricePer100g)} за 100г",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            weight = value
                            isError = false
                        }
                    },
                    label = { Text("Вага (грам)") },
                    placeholder = { Text("Наприклад: 250") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Введіть коректну вагу")
                        } else if (weight.isNotEmpty()) {
                            val weightInt = weight.toIntOrNull() ?: 0
                            val price = product.pricePer100g * weightInt.toBigDecimal() / 100.toBigDecimal()
                            Text(
                                text = "Ціна: ${formatPrice(price)}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val weightInt = weight.toIntOrNull()
                            if (weightInt != null && weightInt > 0) {
                                onConfirm(weightInt)
                                focusManager.clearFocus()
                            } else {
                                isError = true
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick weight buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 250, 500).forEach { quickWeight ->
                        OutlinedButton(
                            onClick = { weight = quickWeight.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${quickWeight}г")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weightInt = weight.toIntOrNull()
                    if (weightInt != null && weightInt > 0) {
                        onConfirm(weightInt)
                    } else {
                        isError = true
                    }
                },
                enabled = weight.isNotEmpty()
            ) {
                Text("Додати")
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
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Спробувати знову")
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
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
