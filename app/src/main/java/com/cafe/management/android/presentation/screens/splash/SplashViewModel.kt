package com.cafe.management.android.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.cafe.management.android.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn()
}
