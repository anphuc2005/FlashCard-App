package com.example.flashcardapp.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.auth.AuthUseCases

class AuthViewModelFactory(private val useCases: AuthUseCases) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(useCases.login) as T
            RegisterViewModel::class.java -> RegisterViewModel(useCases.register) as T
            ForgotPasswordViewModel::class.java -> ForgotPasswordViewModel(useCases.forgotPassword) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

