package com.example.flashcardapp.domain.usecase.auth

import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<RegisterResponse> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu"))
        }
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập họ và tên"))
        }
        
        val request = RegisterRequest(email = email, password = password, displayName = displayName)
        return repository.register(request)
    }
}

