package com.example.flashcardapp.presentation.feature.auth

sealed class AuthOperationState {
    object Idle : AuthOperationState()
    object Loading : AuthOperationState()
    data class Success(
        val message: String = "",
        val email: String? = null
    ) : AuthOperationState()
    data class Error(val message: String) : AuthOperationState()
}

data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null
)

data class RegisterFormState(
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

data class ForgotPasswordFormState(
    val emailError: String? = null
)

data class ResetPasswordFormState(
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

