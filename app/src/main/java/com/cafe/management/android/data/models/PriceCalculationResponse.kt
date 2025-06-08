package com.cafe.management.android.data.models.responses

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PriceCalculationResponse(
    @SerializedName("productId")
    val productId: String,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("pricePer100g")
    val pricePer100g: BigDecimal,
    @SerializedName("weightGrams")
    val weightGrams: Int,
    @SerializedName("totalPrice")
    val totalPrice: BigDecimal
)
