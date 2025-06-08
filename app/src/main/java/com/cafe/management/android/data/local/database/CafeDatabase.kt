package com.cafe.management.android.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cafe.management.android.data.local.database.converters.BigDecimalConverter
import com.cafe.management.android.data.local.database.dao.ProductDao
import com.cafe.management.android.data.local.database.dao.TransactionDao
import com.cafe.management.android.data.local.database.entities.*

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        PendingTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class CafeDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
}
