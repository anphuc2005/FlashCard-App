package com.example.flashcardapp.ui.feature.auth.logic.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

