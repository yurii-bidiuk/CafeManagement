package com.cafe.management.android.presentation.screens.transaction.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.data.models.Transaction
import com.cafe.management.android.data.models.UserRole
import com.cafe.management.android.domain.repository.AuthRepository
import com.cafe.management.android.domain.repository.TransactionRepository
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var currentUserRole: UserRole? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    currentUserId = result.data?.id
                    currentUserRole = result.data?.role
                }
            }
        }
    }

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(transactionId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        result.data?.let { transaction ->
                            val canCancel = canCancelTransaction(transaction)
                            _uiState.update {
                                it.copy(
                                    transaction = transaction,
                                    canCancel = canCancel,
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
                                error = result.message ?: "Помилка завантаження транзакції"
                            )
                        }
                    }
                }
            }
        }
    }

    fun cancelTransaction(transactionId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            transactionRepository.cancelTransaction(transactionId, reason).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                    is Resource.Success -> {
                        Timber.d("Transaction cancelled successfully")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isCancelled = true,
                                transaction = result.data
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Помилка скасування транзакції"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun canCancelTransaction(transaction: Transaction): Boolean {
        // Already cancelled
        if (transaction.isCancelled) return false

        // Check time limit (e.g., can cancel within 30 minutes)
        val transactionTime = try {
            LocalDateTime.parse(transaction.createdAt)
        } catch (e: Exception) {
            return false
        }

        val minutesSinceCreation = ChronoUnit.MINUTES.between(transactionTime, LocalDateTime.now())
        if (minutesSinceCreation > 30) return false

        // Check user permissions
        return when (currentUserRole) {
            UserRole.ADMIN -> true // Admin can cancel any transaction
            UserRole.SELLER -> transaction.createdBy.id == currentUserId // Seller can only cancel own transactions
            null -> false
        }
    }
}

data class TransactionDetailsUiState(
    val transaction: Transaction? = null,
    val canCancel: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCancelled: Boolean = false
)
