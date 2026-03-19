package com.example.flashcardapp.ui.feature.auth.domain

import com.example.flashcardapp.ui.feature.auth.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return repository.forgotPassword(request)
    }
}
