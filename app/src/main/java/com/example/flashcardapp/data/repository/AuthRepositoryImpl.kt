package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.session.AuthSessionStore
import com.example.flashcardapp.data.datasource.remote.auth.AuthRemoteDataSource
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.domain.repository.AuthRepository as DomainAuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionStore: AuthSessionStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DomainAuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(ioDispatcher) {
        remoteDataSource.login(request)
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(ioDispatcher) {
        remoteDataSource.register(request)
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> = withContext(ioDispatcher) {
        remoteDataSource.forgotPassword(request)
    }

    override fun saveLoginSession(accessToken: String?) {
        sessionStore.saveLoginSession(accessToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }
}
