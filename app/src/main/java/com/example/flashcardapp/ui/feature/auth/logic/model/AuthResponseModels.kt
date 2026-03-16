package com.example.flashcardapp.ui.feature.auth.logic.model

data class LoginResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val message: String
)

data class RegisterResponse(
    val userId: String,
    val message: String
)

data class ForgotPasswordResponse(
    val requestId: String,
    val message: String
)

