package com.cafe.management.android.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.management.android.data.models.UserRole
import com.cafe.management.android.domain.repository.AuthRepository
import com.cafe.management.android.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _uiState.update {
                                it.copy(
                                    username = user.username,
                                    userRole = when (user.role) {
                                        UserRole.ADMIN -> "Адміністратор"
                                        UserRole.SELLER -> "Продавець"
                                    },
                                    isAdmin = user.role == UserRole.ADMIN
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Handle error
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}

data class HomeUiState(
    val username: String = "",
    val userRole: String = "",
    val isAdmin: Boolean = false,
    val isLoggedOut: Boolean = false
)
