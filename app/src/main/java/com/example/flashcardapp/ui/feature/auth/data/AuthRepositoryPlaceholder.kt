package com.example.flashcardapp.ui.feature.auth.data

import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse

class AuthRepositoryPlaceholder : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return Result.failure(NotImplementedError("Login API/Firebase not implemented yet"))
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return Result.failure(NotImplementedError("Register API/Firebase not implemented yet"))
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return Result.failure(NotImplementedError("Forgot password API/Firebase not implemented yet"))
    }

    override fun saveLoginSession(accessToken: String?) = Unit

    override fun clearLoginSession() = Unit
}
