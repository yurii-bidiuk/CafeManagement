package com.cafe.management.android.data.models.requests

data class LoginRequest(
    val username: String,
    val password: String,
    val grantType: String = "password",
    val clientId: String = "cafe-client"
)
