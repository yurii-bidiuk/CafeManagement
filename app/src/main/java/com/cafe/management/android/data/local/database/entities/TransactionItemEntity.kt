package com.cafe.management.android.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionItemEntity(
    @PrimaryKey
    val id: String,
    val transactionId: String,
    val productId: String,
    val productName: String,
    val pricePer100g: BigDecimal,
    val weightGrams: Int,
    val totalPrice: BigDecimal
)
