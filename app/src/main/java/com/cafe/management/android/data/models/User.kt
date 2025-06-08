package com.cafe.management.android.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("role")
    val role: UserRole
)

enum class UserRole {
    @SerializedName("ADMIN")
    ADMIN,
    @SerializedName("SELLER")
    SELLER
}
