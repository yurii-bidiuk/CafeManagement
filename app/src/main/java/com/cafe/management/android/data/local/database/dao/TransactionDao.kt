package com.cafe.management.android.data.local.database.dao

import androidx.room.*
import com.cafe.management.android.data.local.database.entities.PendingTransactionEntity
import com.cafe.management.android.data.local.database.entities.TransactionEntity
import com.cafe.management.android.data.local.database.entities.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: String): List<TransactionItemEntity>

    @Query("SELECT * FROM transactions WHERE createdAt >= :startDate AND createdAt < :endDate ORDER BY createdAt DESC")
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TransactionEntity>

    @Query("UPDATE transactions SET isCancelled = :isCancelled, cancellationReason = :reason WHERE id = :transactionId")
    suspend fun updateTransactionCancelled(transactionId: String, isCancelled: Boolean, reason: String)

    // Pending Transactions
    @Insert
    suspend fun insertPendingTransaction(pendingTransaction: PendingTransactionEntity)

    @Query("SELECT * FROM pending_transactions ORDER BY createdAt ASC")
    suspend fun getAllPendingTransactions(): List<PendingTransactionEntity>

    @Query("SELECT COUNT(*) FROM pending_transactions")
    fun getPendingTransactionsCount(): Flow<Int>

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deletePendingTransaction(id: String)

    @Query("DELETE FROM pending_transactions")
    suspend fun deleteAllPendingTransactions()
}
