package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.session.AuthSessionStore
import com.example.flashcardapp.data.datasource.remote.auth.AuthRemoteDataSource
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse

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

    override fun saveLoginSession(accessToken: String?) {
        sessionStore.saveLoginSession(accessToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }
}

