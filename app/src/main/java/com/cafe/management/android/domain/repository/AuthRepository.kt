package com.cafe.management.android.domain.repository

import com.cafe.management.android.data.models.User
import com.cafe.management.android.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<Resource<User>>
    suspend fun logout()
    suspend fun getCurrentUser(): Flow<Resource<User>>
    suspend fun refreshToken(): Flow<Resource<Unit>>
    fun isLoggedIn(): Flow<Boolean>
}
