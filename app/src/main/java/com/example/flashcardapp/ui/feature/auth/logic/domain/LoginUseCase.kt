package com.example.flashcardapp.ui.feature.auth.logic.domain

import com.example.flashcardapp.ui.feature.auth.logic.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.LoginResponse

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): Result<LoginResponse> {
        return repository.login(request)
    }
}

