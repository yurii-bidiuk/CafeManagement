package com.cafe.management.android.data.repository

import com.cafe.management.android.data.api.CafeApi
import com.cafe.management.android.data.api.interceptors.NoConnectivityException
import com.cafe.management.android.data.local.database.dao.TransactionDao
import com.cafe.management.android.data.local.database.entities.PendingTransactionEntity
import com.cafe.management.android.data.local.database.entities.TransactionEntity
import com.cafe.management.android.data.local.database.entities.TransactionItemEntity
import com.cafe.management.android.data.models.Transaction
import com.cafe.management.android.data.models.requests.CreateTransactionRequest
import com.cafe.management.android.domain.repository.TransactionRepository
import com.cafe.management.android.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: CafeApi,
    private val transactionDao: TransactionDao,
    private val gson: Gson
) : TransactionRepository {

    override suspend fun createTransaction(
        request: CreateTransactionRequest
    ): Flow<Resource<Transaction>> = flow {
        emit(Resource.Loading())

        try {
            val transaction = api.createTransaction(request)

            // Save to local database
            saveTransactionToLocal(transaction)

            emit(Resource.Success(transaction))
        } catch (e: NoConnectivityException) {
            // Save as pending transaction for later sync
            savePendingTransaction(request)
            emit(Resource.Error("Транзакцію збережено локально. Буде синхронізовано при відновленні з'єднання"))
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error creating transaction")
            emit(Resource.Error("Помилка створення транзакції: ${e.code()}"))
        } catch (e: IOException) {
            Timber.e(e, "Network error creating transaction")
            // Save as pending transaction
            savePendingTransaction(request)
            emit(Resource.Error("Помилка мережі. Транзакцію збережено локально"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error creating transaction")
            emit(Resource.Error("Несподівана помилка: ${e.message}"))
        }
    }

    override suspend fun getTransactionById(transactionId: String): Flow<Resource<Transaction>> = flow {
        emit(Resource.Loading())

        try {
            val transaction = api.getTransactionById(transactionId)
            emit(Resource.Success(transaction))
        } catch (e: NoConnectivityException) {
            // Try to get from local database
            val localTransaction = transactionDao.getTransactionById(transactionId)
            if (localTransaction != null) {
                val transaction = convertToTransaction(localTransaction)
                emit(Resource.Success(transaction))
            } else {
                emit(Resource.Error("Транзакцію не знайдено"))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emit(Resource.Error("Транзакцію не знайдено"))
                else -> emit(Resource.Error("Помилка сервера: ${e.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun getTodayTransactions(): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())

        try {
            val transactions = api.getTodayTransactions()

            // Save to local database
            transactions.forEach { transaction ->
                saveTransactionToLocal(transaction)
            }

            emit(Resource.Success(transactions))
        } catch (e: NoConnectivityException) {
            // Get from local database
            val startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay()
            val endOfDay = startOfDay.plusDays(1)

            val localTransactions = transactionDao.getTransactionsByDateRange(
                startOfDay.toString(),
                endOfDay.toString()
            )

            val transactions = localTransactions.map { convertToTransaction(it) }
            emit(Resource.Success(transactions))
        } catch (e: Exception) {
            emit(Resource.Error("Помилка отримання транзакцій: ${e.message}"))
        }
    }

    override suspend fun getMyLastTransaction(): Flow<Resource<Transaction>> = flow {
        emit(Resource.Loading())

        try {
            val transaction = api.getMyLastTransaction()
            emit(Resource.Success(transaction))
        } catch (e: NoConnectivityException) {
            emit(Resource.Error("Відсутнє з'єднання з інтернетом"))
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emit(Resource.Error("Транзакцій не знайдено"))
                else -> emit(Resource.Error("Помилка сервера: ${e.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun cancelTransaction(
        transactionId: String,
        reason: String
    ): Flow<Resource<Transaction>> = flow {
        emit(Resource.Loading())

        try {
            val transaction = api.cancelTransaction(
                transactionId = transactionId,
                reason = mapOf("reason" to reason)
            )

            // Update local database
            transactionDao.updateTransactionCancelled(transactionId, true, reason)

            emit(Resource.Success(transaction))
        } catch (e: NoConnectivityException) {
            emit(Resource.Error("Потрібне з'єднання з інтернетом для скасування транзакції"))
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> emit(Resource.Error("Неможливо скасувати цю транзакцію"))
                404 -> emit(Resource.Error("Транзакцію не знайдено"))
                else -> emit(Resource.Error("Помилка сервера: ${e.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun syncPendingTransactions() {
        try {
            val pendingTransactions = transactionDao.getAllPendingTransactions()

            pendingTransactions.forEach { pending ->
                try {
                    // Deserialize request
                    val request = gson.fromJson(pending.requestJson, CreateTransactionRequest::class.java)

                    // Try to send to server
                    val transaction = api.createTransaction(request)

                    // Save successful transaction
                    saveTransactionToLocal(transaction)

                    // Remove from pending
                    transactionDao.deletePendingTransaction(pending.id)

                    Timber.d("Synced pending transaction: ${pending.id}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync pending transaction: ${pending.id}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync pending transactions")
        }
    }

    override suspend fun savePendingTransaction(request: CreateTransactionRequest) {
        try {
            val pendingEntity = PendingTransactionEntity(
                id = UUID.randomUUID().toString(),
                requestJson = gson.toJson(request),
                createdAt = LocalDateTime.now().toString()
            )
            transactionDao.insertPendingTransaction(pendingEntity)
            Timber.d("Saved pending transaction: ${pendingEntity.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save pending transaction")
        }
    }

    override fun getPendingTransactionsCount(): Flow<Int> {
        return transactionDao.getPendingTransactionsCount()
    }

    override suspend fun getTransactionsByDateRange(
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading())

        try {
            val transactions = api.getTransactionsByDateRange(
                from = from.toString(),
                to = to.toString()
            )

            // Save to local database
            transactions.forEach { transaction ->
                saveTransactionToLocal(transaction)
            }

            emit(Resource.Success(transactions))
        } catch (e: NoConnectivityException) {
            // Get from local database
            val localTransactions = transactionDao.getTransactionsByDateRange(
                from.toString(),
                to.toString()
            )

            val transactions = localTransactions.map { convertToTransaction(it) }
            emit(Resource.Success(transactions))
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error getting transactions")
            emit(Resource.Error("Помилка сервера: ${e.code()}"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error getting transactions")
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    private suspend fun saveTransactionToLocal(transaction: Transaction) {
        try {
            val transactionEntity = TransactionEntity(
                id = transaction.id,
                transactionNumber = transaction.transactionNumber,
                totalAmount = transaction.totalAmount,
                discountAmount = transaction.discountAmount,
                finalAmount = transaction.finalAmount,
                paymentMethod = transaction.paymentMethod.name,
                isCancelled = transaction.isCancelled,
                createdAt = transaction.createdAt,
                createdByUsername = transaction.createdBy.username
            )
            transactionDao.insertTransaction(transactionEntity)

            // Save transaction items
            val itemEntities = transaction.items.map { item ->
                TransactionItemEntity(
                    id = item.id,
                    transactionId = transaction.id,
                    productId = item.productId,
                    productName = item.productName,
                    pricePer100g = item.pricePer100g,
                    weightGrams = item.weightGrams,
                    totalPrice = item.totalPrice
                )
            }
            transactionDao.insertTransactionItems(itemEntities)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save transaction to local database")
        }
    }

    private suspend fun convertToTransaction(entity: TransactionEntity): Transaction {
        val items = transactionDao.getTransactionItems(entity.id)

        return Transaction(
            id = entity.id,
            transactionNumber = entity.transactionNumber,
            items = items.map { itemEntity ->
                com.cafe.management.android.data.models.TransactionItem(
                    id = itemEntity.id,
                    productId = itemEntity.productId,
                    productName = itemEntity.productName,
                    pricePer100g = itemEntity.pricePer100g,
                    weightGrams = itemEntity.weightGrams,
                    totalPrice = itemEntity.totalPrice
                )
            },
            totalAmount = entity.totalAmount,
            discountAmount = entity.discountAmount,
            finalAmount = entity.finalAmount,
            paymentMethod = com.cafe.management.android.data.models.PaymentMethod.valueOf(entity.paymentMethod),
            isCancelled = entity.isCancelled,
            createdAt = entity.createdAt,
            createdBy = com.cafe.management.android.data.models.User(
                id = "",
                username = entity.createdByUsername,
                email = "",
                role = com.cafe.management.android.data.models.UserRole.SELLER
            )
        )
    }
}
