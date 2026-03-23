package com.example.flashcardapp.ui.feature.auth.domain

import com.example.flashcardapp.ui.feature.auth.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): Result<LoginResponse> {
        return repository.login(request).onSuccess { response ->
            repository.saveLoginSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
        }
    }
}
