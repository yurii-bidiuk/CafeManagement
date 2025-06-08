package com.cafe.management.android.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    // TODO: Inject ReportRepository when created
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun selectPeriod(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadReports()
    }

    fun refresh() {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Replace with actual API calls
            // Simulating data loading with mock data
            kotlinx.coroutines.delay(1000)

            try {
                val mockData = generateMockData(_uiState.value.selectedPeriod)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        totalRevenue = mockData.totalRevenue,
                        transactionCount = mockData.transactionCount,
                        averageTransaction = mockData.averageTransaction,
                        topProduct = mockData.topProduct,
                        revenueData = mockData.revenueData,
                        topProducts = mockData.topProducts,
                        cashAmount = mockData.cashAmount,
                        cardAmount = mockData.cardAmount,
                        categoryPerformance = mockData.categoryPerformance
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Помилка завантаження звітів"
                    )
                }
            }
        }
    }

    private fun generateMockData(period: ReportPeriod): MockReportData {
        // Generate mock data based on period
        val multiplier = when (period) {
            ReportPeriod.TODAY -> 1
            ReportPeriod.YESTERDAY -> 1
            ReportPeriod.THIS_WEEK -> 7
            ReportPeriod.LAST_WEEK -> 7
            ReportPeriod.THIS_MONTH -> 30
            ReportPeriod.LAST_MONTH -> 30
        }

        val baseRevenue = BigDecimal("5000")
        val totalRevenue = baseRevenue.multiply(BigDecimal(multiplier))
        val transactionCount = 50 * multiplier

        return MockReportData(
            totalRevenue = totalRevenue,
            transactionCount = transactionCount,
            averageTransaction = totalRevenue.divide(BigDecimal(transactionCount), 2, RoundingMode.HALF_UP),
            topProduct = "Капучино",
            revenueData = generateRevenueData(period),
            topProducts = generateTopProducts(),
            cashAmount = totalRevenue.multiply(BigDecimal("0.6")),
            cardAmount = totalRevenue.multiply(BigDecimal("0.4")),
            categoryPerformance = generateCategoryPerformance()
        )
    }

    private fun generateRevenueData(period: ReportPeriod): List<RevenueDataPoint> {
        val days = when (period) {
            ReportPeriod.TODAY, ReportPeriod.YESTERDAY -> 1
            ReportPeriod.THIS_WEEK, ReportPeriod.LAST_WEEK -> 7
            ReportPeriod.THIS_MONTH, ReportPeriod.LAST_MONTH -> 30
        }

        return (0 until days).map { day ->
            RevenueDataPoint(
                date = LocalDate.now().minusDays(day.toLong()),
                amount = BigDecimal((3000..7000).random())
            )
        }.reversed()
    }

    private fun generateTopProducts(): List<ProductPerformance> {
        return listOf(
            ProductPerformance("Капучино", 150, BigDecimal("7500")),
            ProductPerformance("Американо", 120, BigDecimal("4800")),
            ProductPerformance("Латте", 100, BigDecimal("5500")),
            ProductPerformance("Еспресо", 80, BigDecimal("2400")),
            ProductPerformance("Чізкейк", 60, BigDecimal("3600"))
        )
    }

    private fun generateCategoryPerformance(): List<CategoryPerformance> {
        return listOf(
            CategoryPerformance("Кава", 450, BigDecimal("20200")),
            CategoryPerformance("Десерти", 120, BigDecimal("7200")),
            CategoryPerformance("Чай", 80, BigDecimal("2400")),
            CategoryPerformance("Снеки", 50, BigDecimal("1500"))
        )
    }
}

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.TODAY,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalRevenue: BigDecimal = BigDecimal.ZERO,
    val transactionCount: Int = 0,
    val averageTransaction: BigDecimal = BigDecimal.ZERO,
    val topProduct: String? = null,
    val revenueData: List<RevenueDataPoint> = emptyList(),
    val topProducts: List<ProductPerformance> = emptyList(),
    val cashAmount: BigDecimal = BigDecimal.ZERO,
    val cardAmount: BigDecimal = BigDecimal.ZERO,
    val categoryPerformance: List<CategoryPerformance> = emptyList()
)

enum class ReportPeriod {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    LAST_MONTH
}

data class RevenueDataPoint(
    val date: LocalDate,
    val amount: BigDecimal
)

data class ProductPerformance(
    val name: String,
    val quantity: Int,
    val revenue: BigDecimal
)

data class CategoryPerformance(
    val name: String,
    val itemsSold: Int,
    val revenue: BigDecimal
)

private data class MockReportData(
    val totalRevenue: BigDecimal,
    val transactionCount: Int,
    val averageTransaction: BigDecimal,
    val topProduct: String,
    val revenueData: List<RevenueDataPoint>,
    val topProducts: List<ProductPerformance>,
    val cashAmount: BigDecimal,
    val cardAmount: BigDecimal,
    val categoryPerformance: List<CategoryPerformance>
)
