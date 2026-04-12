package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.data.datasource.local.database.FlashCardDatabase
import com.example.flashcardapp.data.datasource.local.session.AuthSessionStoreImpl
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.data.repository.EmailAuthRepositoryImpl
import com.example.flashcardapp.domain.usecase.auth.AuthUseCases
import com.example.flashcardapp.domain.usecase.auth.ForgotPasswordUseCase
import com.example.flashcardapp.domain.usecase.auth.LoginUseCase
import com.example.flashcardapp.domain.usecase.auth.RegisterUseCase
import com.example.flashcardapp.domain.usecase.auth.ResetPasswordUseCase
import com.example.flashcardapp.domain.usecase.auth.VerifyOtpUseCase

/**
 * Dependency Injection Container
 * Khởi tạo 1 lần và dùng chung toàn App (Singleton pattern)
 */
class AppContainer(private val applicationContext: Context) {

    // 1. Core / Managers
    val sessionManager: AppSessionManager by lazy {
        AppSessionManager(applicationContext)
    }

    // 2. Data Stores
    private val authSessionStore by lazy {
        AuthSessionStoreImpl(sessionManager)
    }

    // 3. Repositories
    val authRepository by lazy {
        EmailAuthRepositoryImpl(RetrofitClient.authApiService, authSessionStore)
    }

    val deckRepository: DeckRepository by lazy {
        val database = FlashCardDatabase.getInstance(applicationContext)
        DeckRepository(RetrofitClient.deckApiService, database.deckDao())
    }

    val flashCardRepository: FlashCardRepository by lazy {
        val database = FlashCardDatabase.getInstance(applicationContext)
        FlashCardRepository(RetrofitClient.deckApiService, database.flashCardDao())
    }

    // 4. Use Cases
    val authUseCases: AuthUseCases by lazy {
        AuthUseCases(
            login = LoginUseCase(authRepository),
            register = RegisterUseCase(authRepository),
            forgotPassword = ForgotPasswordUseCase(authRepository),
            verifyOtp = VerifyOtpUseCase(authRepository),
            resetPassword = ResetPasswordUseCase(authRepository)
        )
    }
}
