package com.cafe.management.android.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey
    val id: String,
    val requestJson: String,
    val createdAt: String
)
