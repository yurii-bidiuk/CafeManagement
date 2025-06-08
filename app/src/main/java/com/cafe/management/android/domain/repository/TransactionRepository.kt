package com.cafe.management.android.domain.repository

import com.cafe.management.android.data.models.Transaction
import com.cafe.management.android.data.models.requests.CreateTransactionRequest
import com.cafe.management.android.util.Resource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TransactionRepository {
    suspend fun createTransaction(request: CreateTransactionRequest): Flow<Resource<Transaction>>
    suspend fun getTransactionById(transactionId: String): Flow<Resource<Transaction>>
    suspend fun getTodayTransactions(): Flow<Resource<List<Transaction>>>
    suspend fun getMyLastTransaction(): Flow<Resource<Transaction>>
    suspend fun cancelTransaction(transactionId: String, reason: String): Flow<Resource<Transaction>>
    suspend fun syncPendingTransactions()
    suspend fun savePendingTransaction(request: CreateTransactionRequest)
    fun getPendingTransactionsCount(): Flow<Int>
    suspend fun getTransactionsByDateRange(
        from: LocalDateTime, to: LocalDateTime
    ): Flow<Resource<List<Transaction>>>
}
