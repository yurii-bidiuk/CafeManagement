package com.cafe.management.android.data.api

import com.cafe.management.android.data.models.*
import com.cafe.management.android.data.models.requests.*
import com.cafe.management.android.data.models.responses.*
import retrofit2.http.*

interface CafeApi {

    // Categories
    @GET("api/v1/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/v1/categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: String): Category

    // Products
    @GET("api/v1/products/available")
    suspend fun getAvailableProducts(@Query("categoryId") categoryId: String? = null): List<Product>

    @GET("api/v1/products/{id}")
    suspend fun getProductById(@Path("id") productId: String): Product

    @POST("api/v1/products/{id}/calculate-price")
    suspend fun calculatePrice(
        @Path("id") productId: String,
        @Body request: CalculatePriceRequest
    ): PriceCalculationResponse

    // Transactions
    @POST("api/v1/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Transaction

    @GET("api/v1/transactions/{id}")
    suspend fun getTransactionById(@Path("id") transactionId: String): Transaction

    @GET("api/v1/transactions/today")
    suspend fun getTodayTransactions(): List<Transaction>

    @GET("api/v1/transactions")
    suspend fun getTransactionsByDateRange(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<Transaction>

    @POST("api/v1/transactions/{id}/cancel")
    suspend fun cancelTransaction(
        @Path("id") transactionId: String,
        @Body reason: Map<String, String>
    ): Transaction

    @GET("api/v1/transactions/my-last")
    suspend fun getMyLastTransaction(): Transaction

    @GET("api/v1/transactions/today/statistics")
    suspend fun getTodayStatistics(): Map<String, Any>
}