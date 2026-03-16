package com.example.flashcardapp.ui.feature.auth.logic.domain

import com.example.flashcardapp.ui.feature.auth.logic.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.logic.model.ForgotPasswordResponse

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return repository.forgotPassword(request)
    }
}

