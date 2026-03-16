package com.example.flashcardapp.ui.feature.auth.logic.data

import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse>
}

