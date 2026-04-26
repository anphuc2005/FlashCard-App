package com.example.flashcardapp.domain.usecase.auth

import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu"))
        }
        
        val request = LoginRequest(email = email, password = password)
        val result = repository.login(request)
        
        // Lưu session nếu đăng nhập thành công.
        result.onSuccess { response ->
            repository.saveLoginSession(response.accessToken)
        }
        
        return result
    }
}

