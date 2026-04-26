package com.example.flashcardapp.domain.usecase.auth

import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpResponse
import com.example.flashcardapp.domain.repository.AuthRepository

class VerifyOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String): Result<VerifyOtpResponse> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        if (otp.length < 6) return Result.failure(IllegalArgumentException("Mã OTP không hợp lệ"))

        val request = VerifyOtpRequest(email = email, otpCode = otp)
        return repository.verifyOtp(request)
    }
}


