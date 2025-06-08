package com.cafe.management.android.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val pricePer100g: BigDecimal,
    val isAvailable: Boolean,
    val isCurrentlyAvailable: Boolean,
    val isSeasonal: Boolean
)
