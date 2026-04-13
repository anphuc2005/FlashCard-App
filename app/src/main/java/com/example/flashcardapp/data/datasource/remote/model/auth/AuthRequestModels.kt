package com.example.flashcardapp.data.datasource.remote.model.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val displayName: String,
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otpCode: String
)

data class ResetPasswordRequest(
    val email: String,
    val otpCode: String,
    val newPassword: String
)
