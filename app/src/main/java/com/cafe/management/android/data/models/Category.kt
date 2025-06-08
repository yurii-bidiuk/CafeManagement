package com.cafe.management.android.data.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("displayOrder")
    val displayOrder: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("productCount")
    val productCount: Int? = null
)
