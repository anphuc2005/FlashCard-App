package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse>
    fun saveLoginSession(accessToken: String?)
    fun clearLoginSession()
}

