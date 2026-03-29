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
    val confirmPasswordError: String? = null,
    val strength: PasswordStrength = PasswordStrength.EMPTY
)

enum class PasswordStrength(val label: String, val activeSegments: Int) {
    EMPTY("Nhập mật khẩu", 0),
    WEAK("Yếu", 1),
    FAIR("Trung bình", 2),
    GOOD("Tốt", 3),
    STRONG("Rất mạnh", 4)
}
