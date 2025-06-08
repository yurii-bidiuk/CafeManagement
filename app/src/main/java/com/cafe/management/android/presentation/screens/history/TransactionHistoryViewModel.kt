package com.cafe.management.android.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.data.models.Transaction
import com.cafe.management.android.data.network.NetworkMonitor
import com.cafe.management.android.domain.repository.TransactionRepository
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    val networkStatus: StateFlow<NetworkStatus> = combine(
        networkMonitor.isOnline,
        transactionRepository.getPendingTransactionsCount()
    ) { isOnline, pendingCount ->
        NetworkStatus(
            isOnline = isOnline,
            pendingTransactionsCount = pendingCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkStatus(true, 0)
    )

    init {
        loadTransactions()
        loadTodayStatistics()
    }

    fun refresh() {
        loadTransactions()
        loadTodayStatistics()
    }

    fun setDateFilter(filter: DateFilter) {
        _uiState.update { it.copy(dateFilter = filter) }
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val dateRange = getDateRangeForFilter(_uiState.value.dateFilter)

            transactionRepository.getTransactionsByDateRange(
                from = dateRange.first,
                to = dateRange.second
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        result.data?.let { transactions ->
                            _uiState.update {
                                it.copy(
                                    transactions = transactions,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Помилка завантаження транзакцій"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadTodayStatistics() {
        viewModelScope.launch {
            transactionRepository.getTodayTransactions().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { transactions ->
                            val statistics = calculateStatistics(transactions)
                            _uiState.update {
                                it.copy(todayStatistics = statistics)
                            }
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to load today statistics: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Ignore loading state for statistics
                    }
                }
            }
        }
    }

    private fun getDateRangeForFilter(filter: DateFilter): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()

        return when (filter) {
            DateFilter.TODAY -> {
                today.atStartOfDay() to today.plusDays(1).atStartOfDay()
            }
            DateFilter.YESTERDAY -> {
                today.minusDays(1).atStartOfDay() to today.atStartOfDay()
            }
            DateFilter.THIS_WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                startOfWeek.atStartOfDay() to today.plusDays(1).atStartOfDay()
            }
            DateFilter.THIS_MONTH -> {
                today.withDayOfMonth(1).atStartOfDay() to today.plusDays(1).atStartOfDay()
            }
        }
    }

    private fun calculateStatistics(transactions: List<Transaction>): TodayStatistics {
        val activeTransactions = transactions.filter { !it.isCancelled }
        val cancelledTransactions = transactions.filter { it.isCancelled }

        return TodayStatistics(
            transactionCount = activeTransactions.size,
            totalAmount = activeTransactions.sumOf { it.finalAmount },
            cancelledCount = cancelledTransactions.size
        )
    }
}

data class TransactionHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val dateFilter: DateFilter = DateFilter.TODAY,
    val todayStatistics: TodayStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
