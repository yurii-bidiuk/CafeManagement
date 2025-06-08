package com.cafe.management.android.di

import android.content.Context
import androidx.room.Room
import com.cafe.management.android.data.local.database.CafeDatabase
import com.cafe.management.android.data.local.database.dao.ProductDao
import com.cafe.management.android.data.local.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CafeDatabase = Room.databaseBuilder(
        context,
        CafeDatabase::class.java,
        "cafe_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideProductDao(database: CafeDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: CafeDatabase): TransactionDao = database.transactionDao()
}
