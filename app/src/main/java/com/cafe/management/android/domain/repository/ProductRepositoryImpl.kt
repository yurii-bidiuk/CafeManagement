package com.cafe.management.android.data.repository

import com.cafe.management.android.data.api.CafeApi
import com.cafe.management.android.data.api.interceptors.NoConnectivityException
import com.cafe.management.android.data.local.database.dao.ProductDao
import com.cafe.management.android.data.local.database.entities.CategoryEntity
import com.cafe.management.android.data.local.database.entities.ProductEntity
import com.cafe.management.android.data.models.Category
import com.cafe.management.android.data.models.Product
import com.cafe.management.android.data.models.requests.CalculatePriceRequest
import com.cafe.management.android.data.models.responses.PriceCalculationResponse
import com.cafe.management.android.domain.repository.ProductRepository
import com.cafe.management.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: CafeApi,
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading())

        try {
            // Try to get from API
            val categories = api.getCategories()

            // Save to local database
            val categoryEntities = categories.map { category ->
                CategoryEntity(
                    id = category.id,
                    name = category.name,
                    displayOrder = category.displayOrder,
                    isActive = category.isActive
                )
            }
            productDao.insertCategories(categoryEntities)

            emit(Resource.Success(categories))
        } catch (e: NoConnectivityException) {
            // No internet, try to get from local database
            val localCategories = productDao.getAllCategories()
            if (localCategories.isNotEmpty()) {
                val categories = localCategories.map { entity ->
                    Category(
                        id = entity.id,
                        name = entity.name,
                        displayOrder = entity.displayOrder,
                        isActive = entity.isActive,
                        productCount = null
                    )
                }
                emit(Resource.Success(categories))
            } else {
                emit(Resource.Error("Відсутнє з'єднання з інтернетом"))
            }
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error getting categories")
            emit(Resource.Error("Помилка сервера: ${e.code()}"))
        } catch (e: IOException) {
            Timber.e(e, "Network error getting categories")
            emit(Resource.Error("Помилка мережі"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error getting categories")
            emit(Resource.Error("Несподівана помилка: ${e.message}"))
        }
    }

    override suspend fun getAvailableProducts(categoryId: String?): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())

        try {
            // Try to get from API
            val products = api.getAvailableProducts(categoryId)

            // Save to local database
            val productEntities = products.map { product ->
                ProductEntity(
                    id = product.id,
                    name = product.name,
                    categoryId = product.category.id,
                    categoryName = product.category.name,
                    pricePer100g = product.pricePer100g,
                    isAvailable = product.isAvailable,
                    isCurrentlyAvailable = product.isCurrentlyAvailable,
                    isSeasonal = product.isSeasonal
                )
            }
            productDao.insertProducts(productEntities)

            emit(Resource.Success(products))
        } catch (e: NoConnectivityException) {
            // No internet, try to get from local database
            val localProducts = if (categoryId != null) {
                productDao.getProductsByCategory(categoryId)
            } else {
                productDao.getAllAvailableProducts()
            }

            if (localProducts.isNotEmpty()) {
                val products = localProducts.map { entity ->
                    Product(
                        id = entity.id,
                        name = entity.name,
                        category = com.cafe.management.android.data.models.CategoryShort(
                            id = entity.categoryId,
                            name = entity.categoryName
                        ),
                        pricePer100g = entity.pricePer100g,
                        isAvailable = entity.isAvailable,
                        isCurrentlyAvailable = entity.isCurrentlyAvailable,
                        isSeasonal = entity.isSeasonal
                    )
                }
                emit(Resource.Success(products))
            } else {
                emit(Resource.Error("Відсутнє з'єднання з інтернетом"))
            }
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error getting products")
            emit(Resource.Error("Помилка сервера: ${e.code()}"))
        } catch (e: IOException) {
            Timber.e(e, "Network error getting products")
            emit(Resource.Error("Помилка мережі"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error getting products")
            emit(Resource.Error("Несподівана помилка: ${e.message}"))
        }
    }

    override suspend fun getProductById(productId: String): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())

        try {
            val product = api.getProductById(productId)
            emit(Resource.Success(product))
        } catch (e: NoConnectivityException) {
            // Try to get from local database
            val localProduct = productDao.getProductById(productId)
            if (localProduct != null) {
                val product = Product(
                    id = localProduct.id,
                    name = localProduct.name,
                    category = com.cafe.management.android.data.models.CategoryShort(
                        id = localProduct.categoryId,
                        name = localProduct.categoryName
                    ),
                    pricePer100g = localProduct.pricePer100g,
                    isAvailable = localProduct.isAvailable,
                    isCurrentlyAvailable = localProduct.isCurrentlyAvailable,
                    isSeasonal = localProduct.isSeasonal
                )
                emit(Resource.Success(product))
            } else {
                emit(Resource.Error("Товар не знайдено"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emit(Resource.Error("Товар не знайдено"))
                else -> emit(Resource.Error("Помилка сервера: ${e.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun calculatePrice(
        productId: String,
        weightGrams: Int
    ): Flow<Resource<PriceCalculationResponse>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.calculatePrice(productId, CalculatePriceRequest(weightGrams))
            emit(Resource.Success(response))
        } catch (e: NoConnectivityException) {
            // Calculate locally if no internet
            val product = productDao.getProductById(productId)
            if (product != null) {
                val totalPrice = product.pricePer100g * weightGrams.toBigDecimal() / 100.toBigDecimal()
                val response = PriceCalculationResponse(
                    productId = product.id,
                    productName = product.name,
                    pricePer100g = product.pricePer100g,
                    weightGrams = weightGrams,
                    totalPrice = totalPrice
                )
                emit(Resource.Success(response))
            } else {
                emit(Resource.Error("Товар не знайдено"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Помилка розрахунку ціни"))
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun syncProducts() {
        try {
            val categories = api.getCategories()
            val products = api.getAvailableProducts()

            // Save to local database
            val categoryEntities = categories.map { category ->
                CategoryEntity(
                    id = category.id,
                    name = category.name,
                    displayOrder = category.displayOrder,
                    isActive = category.isActive
                )
            }
            productDao.insertCategories(categoryEntities)

            val productEntities = products.map { product ->
                ProductEntity(
                    id = product.id,
                    name = product.name,
                    categoryId = product.category.id,
                    categoryName = product.category.name,
                    pricePer100g = product.pricePer100g,
                    isAvailable = product.isAvailable,
                    isCurrentlyAvailable = product.isCurrentlyAvailable,
                    isSeasonal = product.isSeasonal
                )
            }
            productDao.insertProducts(productEntities)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync products")
        }
    }
}
