package com.cafe.management.android.data.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    @SerializedName("id")
    val id: String,
    @SerializedName("transactionNumber")
    val transactionNumber: String,
    @SerializedName("items")
    val items: List<TransactionItem>,
    @SerializedName("totalAmount")
    val totalAmount: BigDecimal,
    @SerializedName("discountAmount")
    val discountAmount: BigDecimal,
    @SerializedName("finalAmount")
    val finalAmount: BigDecimal,
    @SerializedName("paymentMethod")
    val paymentMethod: PaymentMethod,
    @SerializedName("isCancelled")
    val isCancelled: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: User
)

data class TransactionItem(
    @SerializedName("id")
    val id: String,
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

enum class PaymentMethod {
    @SerializedName("CASH")
    CASH,
    @SerializedName("CARD")
    CARD
}
