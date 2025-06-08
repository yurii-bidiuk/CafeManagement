package com.cafe.management.android.presentation.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.data.models.PaymentMethod
import com.cafe.management.android.data.models.requests.CreateTransactionRequest
import com.cafe.management.android.data.models.requests.DiscountRequest
import com.cafe.management.android.data.models.requests.DiscountType as ApiDiscountType
import com.cafe.management.android.data.models.requests.TransactionItemRequest
import com.cafe.management.android.domain.repository.TransactionRepository
import com.cafe.management.android.presentation.screens.products.CartItem
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun setCartItems(items: List<CartItem>) {
        _uiState.update { state ->
            state.copy(cartItems = items)
        }
        calculateTotals()
    }

    fun removeFromCart(item: CartItem) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems - item)
        }
        calculateTotals()
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { state ->
            state.copy(paymentMethod = method)
        }
    }

    fun applyDiscount(type: DiscountType, value: BigDecimal) {
        _uiState.update { state ->
            state.copy(discount = Discount(type, value))
        }
        calculateTotals()
    }

    fun removeDiscount() {
        _uiState.update { state ->
            state.copy(discount = null)
        }
        calculateTotals()
    }

    private fun calculateTotals() {
        val items = _uiState.value.cartItems
        val discount = _uiState.value.discount

        val totalAmount = items.sumOf { it.totalPrice }

        val discountAmount = when (discount?.type) {
            DiscountType.PERCENTAGE -> {
                totalAmount * discount.value / BigDecimal(100)
            }
            DiscountType.FIXED -> {
                discount.value.min(totalAmount)
            }
            null -> BigDecimal.ZERO
        }.setScale(2, RoundingMode.HALF_UP)

        val finalAmount = (totalAmount - discountAmount).max(BigDecimal.ZERO)

        _uiState.update { state ->
            state.copy(
                totalAmount = totalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount
            )
        }
    }

    fun createTransaction() {
        if (_uiState.value.cartItems.isEmpty()) {
            _uiState.update { it.copy(error = "Кошик порожній") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val discount = _uiState.value.discount
            val request = CreateTransactionRequest(
                items = _uiState.value.cartItems.map { item ->
                    TransactionItemRequest(
                        productId = item.product.id,
                        weightGrams = item.weightGrams
                    )
                },
                paymentMethod = _uiState.value.paymentMethod,
                discount = discount?.let { d ->
                    DiscountRequest(
                        type = when (d.type) {
                            DiscountType.PERCENTAGE -> ApiDiscountType.PERCENTAGE
                            DiscountType.FIXED -> ApiDiscountType.FIXED
                        },
                        value = d.value
                    )
                }
            )

            transactionRepository.createTransaction(request).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                    is Resource.Success -> {
                        result.data?.let { transaction ->
                            Timber.d("Transaction created successfully: ${transaction.id}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    completedTransactionId = transaction.id
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Failed to create transaction: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Помилка створення транзакції"
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TransactionUiState(
    val cartItems: List<CartItem> = emptyList(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val discount: Discount? = null,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val finalAmount: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val completedTransactionId: String? = null
)
