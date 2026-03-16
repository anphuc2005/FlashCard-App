package com.example.flashcardapp.ui.feature.auth.logic.data

import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterResponse

class AuthRepositoryPlaceholder : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        // TODO: integrate login API here
        return Result.failure(NotImplementedError("Login API not implemented yet"))
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        // TODO: integrate register API here
        return Result.failure(NotImplementedError("Register API not implemented yet"))
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        // TODO: integrate forgot password API here
        return Result.failure(NotImplementedError("Forgot password API not implemented yet"))
    }
}

