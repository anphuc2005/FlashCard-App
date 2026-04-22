package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.data.datasource.local.database.FlashCardDatabase
import com.example.flashcardapp.data.datasource.local.session.AuthSessionStoreImpl
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.data.repository.CategoryRepository
import com.example.flashcardapp.data.repository.EmailAuthRepositoryImpl
import com.example.flashcardapp.domain.usecase.auth.AuthUseCases
import com.example.flashcardapp.domain.usecase.auth.ForgotPasswordUseCase
import com.example.flashcardapp.domain.usecase.auth.GoogleLoginUseCase
import com.example.flashcardapp.domain.usecase.auth.LoginUseCase
import com.example.flashcardapp.domain.usecase.auth.RegisterUseCase
import com.example.flashcardapp.domain.usecase.auth.ResetPasswordUseCase
import com.example.flashcardapp.domain.usecase.auth.VerifyOtpUseCase
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardUseCase
import com.example.flashcardapp.domain.usecase.flashcard.DeleteFlashCardUseCase
import com.example.flashcardapp.domain.usecase.flashcard.UpdateFlashCardUseCase
import com.example.flashcardapp.domain.usecase.deck.AddDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.CloneDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.ExploreDecksUseCase
import com.example.flashcardapp.domain.usecase.deck.GetDeckByIdUseCase
import com.example.flashcardapp.domain.usecase.deck.UpdateDeckUseCase
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.deck.GetExploreDecksFromApiUseCase

/**
 * Dependency Injection Container
 * Khởi tạo 1 lần và dùng chung toàn App (Singleton pattern)
 */
class AppContainer(private val applicationContext: Context) {

    // 1. Core / Managers
    val sessionManager = AppSessionManager(applicationContext)

    init {
        RetrofitClient.tokenProvider = { sessionManager.accessToken }
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

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(RetrofitClient.categoriesApiService)
    }

    val flashCardRepository: FlashCardRepository by lazy {
        val database = FlashCardDatabase.getInstance(applicationContext)
        FlashCardRepository(RetrofitClient.cardApiService, database.flashCardDao())
    }

    // 4. Use Cases
    val authUseCases: AuthUseCases by lazy {
        AuthUseCases(
            login = LoginUseCase(authRepository),
            googleLogin = GoogleLoginUseCase(authRepository),
            register = RegisterUseCase(authRepository),
            forgotPassword = ForgotPasswordUseCase(authRepository),
            verifyOtp = VerifyOtpUseCase(authRepository),
            resetPassword = ResetPasswordUseCase(authRepository)
        )
    }

    val addFlashCardUseCase: AddFlashCardUseCase by lazy {
        AddFlashCardUseCase(flashCardRepository)
    }

    val updateFlashCardUseCase: UpdateFlashCardUseCase by lazy {
        UpdateFlashCardUseCase(flashCardRepository)
    }

    val deleteFlashCardUseCase: DeleteFlashCardUseCase by lazy {
        DeleteFlashCardUseCase(flashCardRepository)
    }

    val addDeckUseCase: AddDeckUseCase by lazy {
        AddDeckUseCase(deckRepository)
    }

    val exploreDecksUseCase: ExploreDecksUseCase by lazy {
        ExploreDecksUseCase(deckRepository)
    }

    val cloneDeckUseCase: CloneDeckUseCase by lazy {
        CloneDeckUseCase(deckRepository)
    }

    val getAllDecksFromApiUseCase: GetExploreDecksFromApiUseCase by lazy {
        GetExploreDecksFromApiUseCase(deckRepository)
    }

    val getAllCategoriesUseCase: GetAllCategoriesUseCase by lazy {
        GetAllCategoriesUseCase(categoryRepository)
    }

    val getDeckByIdUseCase: GetDeckByIdUseCase by lazy {
        GetDeckByIdUseCase(deckRepository)
    }

    val updateDeckUseCase: UpdateDeckUseCase by lazy {
        UpdateDeckUseCase(deckRepository)
    }

    val getCardsByDeckIdUseCase: com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase by lazy {
        com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase(flashCardRepository)
    }
}
