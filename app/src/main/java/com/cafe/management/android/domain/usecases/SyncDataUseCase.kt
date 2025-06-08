package com.cafe.management.android.domain.usecases

import com.cafe.management.android.domain.repository.ProductRepository
import com.cafe.management.android.domain.repository.TransactionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke() = coroutineScope {
        try {
            Timber.d("Starting data sync")

            // Sync in parallel
            val productsDeferred = async { productRepository.syncProducts() }
            val transactionsDeferred = async { transactionRepository.syncPendingTransactions() }

            // Wait for both to complete
            productsDeferred.await()
            transactionsDeferred.await()

            Timber.d("Data sync completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error during data sync")
        }
    }
}
