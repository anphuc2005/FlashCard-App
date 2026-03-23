package com.example.flashcardapp.ui.feature.auth.domain

import com.example.flashcardapp.ui.feature.auth.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterRequest): Result<RegisterResponse> {
        return repository.register(request)
    }
}
