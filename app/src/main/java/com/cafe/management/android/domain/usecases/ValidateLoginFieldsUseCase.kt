package com.cafe.management.android.domain.usecases

import javax.inject.Inject

class ValidateLoginFieldsUseCase @Inject constructor() {

    operator fun invoke(
        username: String,
        password: String
    ): ValidationResult {
        val usernameError = when {
            username.isBlank() -> "Введіть ім'я користувача"
            username.length < 3 -> "Ім'я користувача має містити мінімум 3 символи"
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> "Введіть пароль"
            password.length < 4 -> "Пароль має містити мінімум 4 символи"
            else -> null
        }

        return ValidationResult(
            isValid = usernameError == null && passwordError == null,
            usernameError = usernameError,
            passwordError = passwordError
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val usernameError: String? = null,
        val passwordError: String? = null
    )
}
