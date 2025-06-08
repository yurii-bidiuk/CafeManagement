package com.cafe.management.android.data.models.requests

import com.cafe.management.android.data.models.PaymentMethod
import java.math.BigDecimal

data class CreateTransactionRequest(
    val items: List<TransactionItemRequest>,
    val paymentMethod: PaymentMethod,
    val discount: DiscountRequest? = null
)

data class TransactionItemRequest(
    val productId: String,
    val weightGrams: Int
)

data class DiscountRequest(
    val type: DiscountType,
    val value: BigDecimal
)

enum class DiscountType {
    PERCENTAGE,
    FIXED
}
