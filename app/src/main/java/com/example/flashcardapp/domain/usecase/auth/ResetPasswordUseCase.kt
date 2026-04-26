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
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        if (otp.length < 6) return Result.failure(IllegalArgumentException("Mã OTP không hợp lệ"))
        if (newPassword.length < 6) return Result.failure(IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự"))

        val request = ResetPasswordRequest(
            email = email,
            otpCode = otp,
            newPassword = newPassword
        )
        return repository.resetPassword(request)
    }
}


