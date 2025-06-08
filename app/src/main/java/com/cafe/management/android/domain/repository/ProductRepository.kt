package com.cafe.management.android.domain.repository

import com.cafe.management.android.data.models.Category
import com.cafe.management.android.data.models.Product
import com.cafe.management.android.data.models.responses.PriceCalculationResponse
import com.cafe.management.android.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getCategories(): Flow<Resource<List<Category>>>
    suspend fun getAvailableProducts(categoryId: String? = null): Flow<Resource<List<Product>>>
    suspend fun getProductById(productId: String): Flow<Resource<Product>>
    suspend fun calculatePrice(productId: String, weightGrams: Int): Flow<Resource<PriceCalculationResponse>>
    suspend fun syncProducts()
}
