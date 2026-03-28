package com.example.flashcardapp.data.datasource.remote.model.auth

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String?,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val isNewUser: Boolean? = false
)

typealias LoginResponse = AuthResponse
typealias RegisterResponse = AuthResponse

data class ForgotPasswordResponse(
    val requestId: String,
    val message: String
)

