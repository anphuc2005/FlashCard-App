package com.example.flashcardapp.data.datasource.remote.model.auth

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

