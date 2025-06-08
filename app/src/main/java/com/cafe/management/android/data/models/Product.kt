package com.cafe.management.android.data.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Product(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: CategoryShort,
    @SerializedName("pricePer100g")
    val pricePer100g: BigDecimal,
    @SerializedName("isAvailable")
    val isAvailable: Boolean,
    @SerializedName("isCurrentlyAvailable")
    val isCurrentlyAvailable: Boolean,
    @SerializedName("isSeasonal")
    val isSeasonal: Boolean
)

data class CategoryShort(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)
