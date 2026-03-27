package com.example.flashcardapp.data.datasource.remote.model.auth

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

