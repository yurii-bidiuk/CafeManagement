package com.cafe.management.android.data.local.database.dao

import androidx.room.*
import com.cafe.management.android.data.local.database.entities.CategoryEntity
import com.cafe.management.android.data.local.database.entities.ProductEntity

@Dao
interface ProductDao {

    // Categories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    // Products
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM products WHERE isCurrentlyAvailable = 1")
    suspend fun getAllAvailableProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isCurrentlyAvailable = 1")
    suspend fun getProductsByCategory(categoryId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}
