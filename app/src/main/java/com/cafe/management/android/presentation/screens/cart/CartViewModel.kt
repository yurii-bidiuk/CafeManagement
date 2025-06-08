//package com.cafe.management.android.presentation.screens.cart
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cafe.management.android.data.models.PaymentMethod
//import com.cafe.management.android.data.models.requests.CreateTransactionRequest
//import com.cafe.management.android.data.models.requests.TransactionItemRequest
//import com.cafe.management.android.domain.repository.TransactionRepository
//import com.cafe.management.android.presentation.screens.products.CartItem
//import com.cafe.management.android.util.Resource
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import timber.log.Timber
//import java.math.BigDecimal
//import javax.inject.Inject
//
//@HiltViewModel
//class CartViewModel @Inject constructor(
//    private val transactionRepository: TransactionRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(CartUiState())
//    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
//
//    // This should be shared with ProductsViewModel in a real app
//    // For now, we'll simulate getting cart items
//    private val cartItems = mutableListOf<CartItem>()
//
//    companion object {
//        // Shared cart storage - in production this should be in a shared repository
//        val sharedCartItems = mutableListOf<CartItem>()
//    }
//
//    fun loadCartItems() {
//        cartItems.clear()
//        cartItems.addAll(sharedCartItems)
//        updateUiState()
//    }
//
//    fun removeFromCart(index: Int) {
//        if (index in cartItems.indices) {
//            cartItems.removeAt(index)
//            sharedCartItems.clear()
//            sharedCartItems.addAll(cartItems)
//            updateUiState()
//            Timber.d("Removed item at index $index from cart")
//        }
//    }
//
//    fun updateCartItemWeight(index: Int, newWeight: Int) {
//        if (index in cartItems.indices && newWeight > 0) {
//            val item = cartItems[index]
//            val newPrice = item.pricePer100g * BigDecimal(newWeight) / BigDecimal(100)
//
//            val updatedItem = item.copy(
//                weightGrams = newWeight,
//                totalPrice = newPrice
//            )
//
//            cartItems[index] = updatedItem
//            sharedCartItems[index] = updatedItem
//            updateUiState()
//
//            Timber.d("Updated item weight: ${item.productName}, ${newWeight}g, ${newPrice}₴")
//        }
//    }
//
//    fun clearCart() {
//        cartItems.clear()
//        sharedCartItems.clear()
//        updateUiState()
//        Timber.d("Cart cleared")
//    }
//
//    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
//        _uiState.update {
//            it.copy(selectedPaymentMethod = paymentMethod)
//        }
//    }
//
//    fun createTransaction() {
//        if (cartItems.isEmpty()) {
//            _uiState.update {
//                it.copy(error = "Кошик порожній")
//            }
//            return
//        }
//
//        viewModelScope.launch {
//            _uiState.update { it.copy(isProcessing = true) }
//
//            try {
//                val request = CreateTransactionRequest(
//                    items = cartItems.map { cartItem ->
//                        TransactionItemRequest(
//                            productId = cartItem.productId,
//                            weightGrams = cartItem.weightGrams
//                        )
//                    },
//                    paymentMethod = _uiState.value.selectedPaymentMethod
//                )
//
//                transactionRepository.createTransaction(request).collect { result ->
//                    when (result) {
//                        is Resource.Success -> {
//                            Timber.d("Transaction created successfully: ${result.data?.id}")
//                            clearCart()
//                            _uiState.update {
//                                it.copy(
//                                    isProcessing = false,
//                                    transactionCreated = true,
//                                    error = null
//                                )
//                            }
//                        }
//                        is Resource.Error -> {
//                            Timber.e("Transaction creation failed: ${result.message}")
//                            _uiState.update {
//                                it.copy(
//                                    isProcessing = false,
//                                    error = result.message ?: "Помилка створення транзакції"
//                                )
//                            }
//                        }
//                        is Resource.Loading -> {
//                            _uiState.update { it.copy(isProcessing = true) }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Timber.e(e, "Error creating transaction")
//                _uiState.update {
//                    it.copy(
//                        isProcessing = false,
//                        error = "Несподівана помилка: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//
//    private fun updateUiState() {
//        val total = cartItems.sumOf { it.totalPrice }
//        _uiState.update {
//            it.copy(
//                cartItems = cartItems.toList(),
//                total = total,
//                isLoading = false
//            )
//        }
//    }
//
//    // Static method to add items from other screens
//    companion object {
//        fun addItemToSharedCart(item: CartItem) {
//            sharedCartItems.add(item)
//            Timber.d("Added item to shared cart: ${item.productName}")
//        }
//
//        fun getSharedCartItemsCount(): Int = sharedCartItems.size
//
//        fun getSharedCartTotal(): BigDecimal = sharedCartItems.sumOf { it.totalPrice }
//    }
//}
//
//data class CartUiState(
//    val isLoading: Boolean = false,
//    val cartItems: List<CartItem> = emptyList(),
//    val total: BigDecimal = BigDecimal.ZERO,
//    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
//    val isProcessing: Boolean = false,
//    val transactionCreated: Boolean = false,
//    val error: String? = null
//)
