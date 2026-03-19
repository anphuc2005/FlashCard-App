package com.example.flashcardapp.ui.feature.auth.di

import com.example.flashcardapp.ui.feature.auth.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.data.AuthRepositoryPlaceholder
import com.example.flashcardapp.ui.feature.auth.presentation.AuthViewModelFactory

object AuthDependencyProvider {

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryPlaceholder()
    }

    fun provideViewModelFactory(): AuthViewModelFactory {
        return AuthViewModelFactory(authRepository)
    }
}
