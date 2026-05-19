package com.example.flashcardapp.domain.usecase.auth

import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginResponse
import com.example.flashcardapp.domain.repository.AuthRepository

class GoogleLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<GoogleLoginResponse> {
        val request = GoogleLoginRequest(idToken = idToken)
        val result = authRepository.googleLogin(request)
        
        result.onSuccess {
            authRepository.saveLoginSession(it.accessToken)
        }
        
        return result
    }
}
