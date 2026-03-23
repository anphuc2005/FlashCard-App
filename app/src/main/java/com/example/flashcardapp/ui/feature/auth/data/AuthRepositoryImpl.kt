package com.example.flashcardapp.ui.feature.auth.data

import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionStore: AuthSessionStore
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return remoteDataSource.login(request)
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return remoteDataSource.register(request)
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return remoteDataSource.forgotPassword(request)
    }

    override fun saveLoginSession(accessToken: String?, refreshToken: String?) {
        sessionStore.saveLoginSession(accessToken, refreshToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }
}
