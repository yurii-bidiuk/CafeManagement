package com.cafe.management.android.data.repository

import com.cafe.management.android.BuildConfig
import com.cafe.management.android.data.api.AuthApi
import com.cafe.management.android.data.api.interceptors.NoConnectivityException
import com.cafe.management.android.data.local.preferences.TokenManager
import com.cafe.management.android.data.models.User
import com.cafe.management.android.domain.repository.AuthRepository
import com.cafe.management.android.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        try {
            // Login to Keycloak
            val tokenResponse = authApi.login(
                realm = BuildConfig.REALM,
                grantType = "password",
                clientId = BuildConfig.CLIENT_ID,
                username = username,
                password = password
            )

            // Save tokens
            tokenManager.saveTokens(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken
            )

            // Get user info
            val user = authApi.getCurrentUser()

            emit(Resource.Success(user))
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error during login")
            val errorMessage = when (e.code()) {
                401 -> "Невірне ім'я користувача або пароль"
                403 -> "Доступ заборонено"
                else -> "Помилка сервера: ${e.code()}"
            }
            emit(Resource.Error(errorMessage))
        } catch (e: NoConnectivityException) {
            Timber.e(e, "No internet connection")
            emit(Resource.Error("Відсутнє з'єднання з інтернетом"))
        } catch (e: IOException) {
            Timber.e(e, "Network error during login")
            emit(Resource.Error("Помилка мережі. Спробуйте пізніше"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during login")
            emit(Resource.Error("Несподівана помилка: ${e.message}"))
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    override suspend fun getCurrentUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        try {
            val user = authApi.getCurrentUser()
            emit(Resource.Success(user))
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // Token expired, try to refresh
                refreshToken().collect { refreshResult ->
                    when (refreshResult) {
                        is Resource.Success -> {
                            // Retry getting user
                            try {
                                val user = authApi.getCurrentUser()
                                emit(Resource.Success(user))
                            } catch (e: Exception) {
                                emit(Resource.Error("Не вдалося отримати дані користувача"))
                            }
                        }
                        is Resource.Error -> {
                            emit(Resource.Error(refreshResult.message ?: "Помилка авторизації"))
                        }
                        is Resource.Loading -> {}
                    }
                }
            } else {
                emit(Resource.Error("Помилка отримання даних користувача"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Помилка: ${e.message}"))
        }
    }

    override suspend fun refreshToken(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                emit(Resource.Error("Відсутній refresh token"))
                return@flow
            }

            val tokenResponse = authApi.refreshToken(
                realm = BuildConfig.REALM,
                clientId = BuildConfig.CLIENT_ID,
                refreshToken = refreshToken
            )

            tokenManager.saveTokens(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken
            )

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh token")
            emit(Resource.Error("Не вдалося оновити токен"))
            // Clear tokens to force re-login
            tokenManager.clearTokens()
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = tokenManager.isLoggedIn()
}
