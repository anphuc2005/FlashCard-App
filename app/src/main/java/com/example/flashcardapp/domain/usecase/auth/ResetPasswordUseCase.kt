package com.example.flashcardapp.domain.usecase.auth

import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordResponse
import com.example.flashcardapp.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        otp: String,
        newPassword: String
    ): Result<ResetPasswordResponse> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (otp.length < 6) return Result.failure(IllegalArgumentException("OTP is invalid"))
        if (newPassword.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        val request = ResetPasswordRequest(
            email = email,
            otpCode = otp,
            newPassword = newPassword
        )
        return repository.resetPassword(request)
    }
}


