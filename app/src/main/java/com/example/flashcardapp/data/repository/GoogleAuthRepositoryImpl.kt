package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.local.session.AuthSessionStore
import com.example.flashcardapp.data.datasource.remote.api.AuthApiService
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginResponse
import com.example.flashcardapp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val sessionStore: AuthSessionStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(ioDispatcher) {
        Result.failure(NotImplementedError("Google login is not yet implemented via API"))
    }

    override suspend fun googleLogin(request: GoogleLoginRequest): Result<GoogleLoginResponse> = withContext(ioDispatcher) {
        try {
            val response = authApiService.googleLogin(request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth does not support manual registration"))
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth does not support forgot password"))
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth does not support verifying OTP"))
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth does not support password resetting"))
    }

    override fun saveLoginSession(accessToken: String?) {
        sessionStore.saveLoginSession(accessToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }
}
