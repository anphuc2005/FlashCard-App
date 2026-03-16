package com.example.flashcardapp.ui.feature.auth.logic.domain

import com.example.flashcardapp.ui.feature.auth.logic.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.RegisterResponse

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterRequest): Result<RegisterResponse> {
        return repository.register(request)
    }
}

