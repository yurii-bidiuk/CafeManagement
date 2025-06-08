package com.cafe.management.android.presentation.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Звіти") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: Export */ },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Експорт")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Period selector
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodChange = viewModel::selectPeriod,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                val errorMessage = uiState.error
                if (errorMessage != null) {
                    ErrorMessage(
                        message = errorMessage,
                        onRetry = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary cards
                    item {
                        SummaryCards(
                            totalRevenue = uiState.totalRevenue,
                            transactionCount = uiState.transactionCount,
                            averageTransaction = uiState.averageTransaction,
                            topProduct = uiState.topProduct
                        )
                    }

                    // Revenue chart
                    item {
                        RevenueChartCard(
                            data = uiState.revenueData,
                            period = uiState.selectedPeriod
                        )
                    }

                    // Top products
                    item {
                        TopProductsCard(
                            products = uiState.topProducts
                        )
                    }

                    // Payment methods breakdown
                    item {
                        PaymentMethodsCard(
                            cashAmount = uiState.cashAmount,
                            cardAmount = uiState.cardAmount
                        )
                    }

                    // Category performance
                    item {
                        CategoryPerformanceCard(
                            categories = uiState.categoryPerformance
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodChange: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ReportPeriod.values().toList()) { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodChange(period) },
                label = {
                    Text(
                        text = when (period) {
                            ReportPeriod.TODAY -> "Сьогодні"
                            ReportPeriod.YESTERDAY -> "Вчора"
                            ReportPeriod.THIS_WEEK -> "Цей тиждень"
                            ReportPeriod.LAST_WEEK -> "Минулий тиждень"
                            ReportPeriod.THIS_MONTH -> "Цей місяць"
                            ReportPeriod.LAST_MONTH -> "Минулий місяць"
                        }
                    )
                },
                leadingIcon = if (selectedPeriod == period) {
                    {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun SummaryCards(
    totalRevenue: BigDecimal,
    transactionCount: Int,
    averageTransaction: BigDecimal,
    topProduct: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Загальний дохід",
                value = formatPrice(totalRevenue),
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Транзакцій",
                value = transactionCount.toString(),
                icon = Icons.Default.Receipt,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Середній чек",
                value = formatPrice(averageTransaction),
                icon = Icons.Default.Calculate,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            if (topProduct != null) {
                SummaryCard(
                    title = "Топ товар",
                    value = topProduct,
                    icon = Icons.Default.Star,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    isSmallText = true
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isSmallText: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = if (isSmallText) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun RevenueChartCard(
    data: List<RevenueDataPoint>,
    period: ReportPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Динаміка доходу",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart visualization
            if (data.isNotEmpty()) {
                val maxValue = data.maxOfOrNull { it.amount } ?: BigDecimal.ONE

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.takeLast(7).forEach { dataPoint ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateForChart(dataPoint.date, period),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(60.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(
                                            fraction = (dataPoint.amount.toFloat() / maxValue.toFloat())
                                                .coerceIn(0f, 1f)
                                        )
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }

                            Text(
                                text = formatCompactPrice(dataPoint.amount),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Немає даних",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TopProductsCard(
    products: List<ProductPerformance>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Топ товари",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            products.forEachIndexed { index, product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (index) {
                            0 -> Color(0xFFFFD700)
                            1 -> Color(0xFFC0C0C0)
                            2 -> Color(0xFFCD7F32)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${product.quantity} шт",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = formatPrice(product.revenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (index < products.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodsCard(
    cashAmount: BigDecimal,
    cardAmount: BigDecimal
) {
    val total = cashAmount + cardAmount
    val cashPercentage = if (total > BigDecimal.ZERO) {
        ((cashAmount.toFloat() / total.toFloat()) * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Способи оплати",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (cashPercentage > 0) {
                        Box(
                            modifier = Modifier
                                .weight(cashPercentage.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF4CAF50))
                        )
                    }
                    if (cashPercentage < 100) {
                        Box(
                            modifier = Modifier
                                .weight((100 - cashPercentage).toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF2196F3))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PaymentMethodItem(
                    icon = Icons.Default.Money,
                    label = "Готівка",
                    amount = cashAmount,
                    percentage = cashPercentage,
                    color = Color(0xFF4CAF50)
                )
                PaymentMethodItem(
                    icon = Icons.Default.CreditCard,
                    label = "Картка",
                    amount = cardAmount,
                    percentage = 100 - cashPercentage,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amount: BigDecimal,
    percentage: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formatPrice(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryPerformanceCard(
    categories: List<CategoryPerformance>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Продажі по категоріях",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            categories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${category.itemsSold} товарів",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = formatPrice(category.revenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
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

private fun formatPrice(price: BigDecimal): String {
    val format = NumberFormat.getCurrencyInstance(Locale("uk", "UA"))
    return format.format(price)
}

private fun formatCompactPrice(price: BigDecimal): String {
    return when {
        price >= BigDecimal(1000000) -> "${price.divide(BigDecimal(1000000))}M"
        price >= BigDecimal(1000) -> "${price.divide(BigDecimal(1000))}K"
        else -> price.toPlainString()
    }
}

private fun formatDateForChart(date: LocalDate, period: ReportPeriod): String {
    return when (period) {
        ReportPeriod.TODAY, ReportPeriod.YESTERDAY -> date.format(DateTimeFormatter.ofPattern("HH:00"))
        ReportPeriod.THIS_WEEK, ReportPeriod.LAST_WEEK -> date.format(DateTimeFormatter.ofPattern("EEE"))
        ReportPeriod.THIS_MONTH, ReportPeriod.LAST_MONTH -> date.format(DateTimeFormatter.ofPattern("dd"))
    }
}
