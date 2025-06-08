package com.cafe.management.android.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.data.models.Category
import com.cafe.management.android.data.models.Product
import com.cafe.management.android.domain.repository.ProductRepository
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItemsCount: StateFlow<Int> = _cartItems
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        loadData()
    }

    fun loadData() {
        loadCategories()
        loadProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            productRepository.getCategories().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        result.data?.let { categories ->
                            _uiState.update {
                                it.copy(
                                    categories = categories,
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
                                error = result.message ?: "Помилка завантаження категорій"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val categoryId = _uiState.value.selectedCategory?.id

            productRepository.getAvailableProducts(categoryId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        result.data?.let { products ->
                            _uiState.update {
                                it.copy(
                                    allProducts = products,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            filterProducts()
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Помилка завантаження товарів"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCategorySelect(category: Category?) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
        loadProducts()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        filterProducts()
    }

    private fun filterProducts() {
        val query = _uiState.value.searchQuery.lowercase()
        val allProducts = _uiState.value.allProducts

        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.lowercase().contains(query) ||
                        product.category.name.lowercase().contains(query)
            }
        }

        _uiState.update {
            it.copy(filteredProducts = filtered)
        }
    }

    fun onProductSelect(product: Product) {
        if (!product.isCurrentlyAvailable) {
            _uiState.update {
                it.copy(error = "Цей товар тимчасово недоступний")
            }
            return
        }

        _uiState.update {
            it.copy(selectedProduct = product)
        }
    }

    fun clearSelectedProduct() {
        _uiState.update {
            it.copy(selectedProduct = null)
        }
    }

    fun addToCart(product: Product, weightGrams: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingPrice = true) }

            productRepository.calculatePrice(product.id, weightGrams).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already showing loading
                    }
                    is Resource.Success -> {
                        result.data?.let { priceResponse ->
                            val cartItem = CartItem(
                                product = product,
                                weightGrams = weightGrams,
                                totalPrice = priceResponse.totalPrice
                            )

                            _cartItems.update { currentItems ->
                                currentItems + cartItem
                            }

                            _uiState.update {
                                it.copy(
                                    isCalculatingPrice = false,
                                    selectedProduct = null
                                )
                            }

                            Timber.d("Added to cart: ${product.name}, ${weightGrams}g")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isCalculatingPrice = false,
                                selectedProduct = null,
                                error = result.message ?: "Помилка розрахунку ціни"
                            )
                        }
                    }
                }
            }
        }
    }

    fun getCartItems(): List<CartItem> = _cartItems.value

    fun clearCart() {
        _cartItems.update { emptyList() }
    }
}

data class ProductsUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedProduct: Product? = null,
    val isLoading: Boolean = false,
    val isCalculatingPrice: Boolean = false,
    val error: String? = null
)

data class CartItem(
    val product: Product,
    val weightGrams: Int,
    val totalPrice: java.math.BigDecimal
)