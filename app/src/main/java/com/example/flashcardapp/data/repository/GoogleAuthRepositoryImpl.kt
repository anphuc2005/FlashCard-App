package com.example.flashcardapp.data.repository

import com.example.flashcardapp.core.utils.UserMessageMapper
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
        Result.failure(NotImplementedError("Đăng nhập Google qua API chưa được hỗ trợ ở luồng này"))
    }

    override suspend fun googleLogin(request: GoogleLoginRequest): Result<GoogleLoginResponse> = withContext(ioDispatcher) {
        try {
            val response = authApiService.googleLogin(request)
            if (response.isSuccessful && response.body()?.isSuccess() == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(
                    Exception(
                        UserMessageMapper.extractReadableMessage(response.body()?.message)
                            ?: "Đăng nhập Google thất bại"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth không hỗ trợ đăng ký thủ công"))
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth không hỗ trợ quên mật khẩu"))
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth không hỗ trợ xác minh OTP"))
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> {
        return Result.failure(UnsupportedOperationException("Google Auth không hỗ trợ đặt lại mật khẩu"))
    }

    override fun saveLoginSession(accessToken: String?) {
        sessionStore.saveLoginSession(accessToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }
}
