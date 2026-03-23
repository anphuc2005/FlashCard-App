package com.example.flashcardapp.ui.feature.auth.data

import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse>
}
