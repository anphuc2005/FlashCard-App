package com.example.flashcardapp.domain.repository

import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginResponse
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

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun googleLogin(request: GoogleLoginRequest): Result<GoogleLoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse>
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse>
    fun saveLoginSession(accessToken: String?, refreshToken: String? = null)
    fun clearLoginSession()
}
