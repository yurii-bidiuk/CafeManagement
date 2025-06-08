// presentation/screens/login/LoginViewModel.kt
package com.cafe.management.android.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.domain.repository.AuthRepository
import com.cafe.management.android.domain.usecases.ValidateLoginFieldsUseCase
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateLoginFields: ValidateLoginFieldsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun onRememberMeChange(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun login() {
        viewModelScope.launch {
            // Validate fields
            val validationResult = validateLoginFields(
                username = _uiState.value.username,
                password = _uiState.value.password
            )

            if (!validationResult.isValid) {
                _uiState.update { currentState ->
                    currentState.copy(
                        usernameError = validationResult.usernameError,
                        passwordError = validationResult.passwordError
                    )
                }
                return@launch
            }

            // Perform login
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            authRepository.login(
                username = _uiState.value.username,
                password = _uiState.value.password
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Timber.d("Login successful")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccessful = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Login failed: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                generalError = result.message ?: "Помилка входу"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false
)