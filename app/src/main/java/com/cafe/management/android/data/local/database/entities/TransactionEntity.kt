package com.cafe.management.android.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val transactionNumber: String,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val paymentMethod: String,
    val isCancelled: Boolean,
    val cancellationReason: String? = null,
    val createdAt: String,
    val createdByUsername: String
)
