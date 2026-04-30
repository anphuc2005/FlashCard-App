package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.data.datasource.local.database.FlashCardDatabase
import com.example.flashcardapp.data.datasource.local.session.AuthSessionStoreImpl
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.data.repository.CategoryRepository
import com.example.flashcardapp.data.repository.EmailAuthRepositoryImpl
import com.example.flashcardapp.data.repository.ProfileRepository
import com.example.flashcardapp.data.repository.StatisticsRepository
import com.example.flashcardapp.data.repository.UploadRepositoryImpl
import com.example.flashcardapp.domain.repository.UploadRepository
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
import com.example.flashcardapp.domain.usecase.profile.GetMyProfileUseCase
import com.example.flashcardapp.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase
import com.example.flashcardapp.domain.usecase.study.GetStudySessionCardsUseCase
import com.example.flashcardapp.domain.usecase.study.GetReviewedCardIdsUseCase
import com.example.flashcardapp.domain.usecase.study.GetCurrentStudyStreakUseCase
import com.example.flashcardapp.domain.usecase.study.HasStudiedTodayUseCase
import com.example.flashcardapp.domain.usecase.study.SaveStudyReviewUseCase
import com.example.flashcardapp.domain.usecase.study.StudyUseCases
import com.example.flashcardapp.domain.usecase.study.SyncStudyReviewsUseCase

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
        DeckRepository(RetrofitClient.deckApiService)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(RetrofitClient.categoriesApiService)
    }

    val flashCardRepository: FlashCardRepository by lazy {
        val database = FlashCardDatabase.getInstance(applicationContext)
        FlashCardRepository(RetrofitClient.cardApiService, database.flashCardDao())
    }

    val uploadRepository: UploadRepository by lazy {
        UploadRepositoryImpl(RetrofitClient.uploadApiService)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(RetrofitClient.profileApiService)
    }

    val statisticsRepository: StatisticsRepository by lazy {
        StatisticsRepository(RetrofitClient.statisticsApiService)
    }

        val studyRepository: StudyRepository by lazy {
            val database = FlashCardDatabase.getInstance(applicationContext)
            StudyRepository(
                RetrofitClient.studyApiService,
                database.studyReviewDao(),
                database.flashCardDao(),
                applicationContext
            )
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
            com.example.flashcardapp.domain.usecase.flashcard.GetCardsByDeckIdUseCase(
                flashCardRepository
            )
        }

        val uploadImageUseCase: UploadImageUseCase by lazy {
            UploadImageUseCase(uploadRepository)
        }

        val getMyProfileUseCase: GetMyProfileUseCase by lazy {
            GetMyProfileUseCase(profileRepository)
        }

        val updateMyProfileUseCase: UpdateMyProfileUseCase by lazy {
            UpdateMyProfileUseCase(profileRepository)
        }

        val studyUseCases: StudyUseCases by lazy {
            StudyUseCases(
                getSessionCards = GetStudySessionCardsUseCase(studyRepository),
                getReviewedCardIds = GetReviewedCardIdsUseCase(studyRepository),
                saveReview = SaveStudyReviewUseCase(studyRepository),
                syncReviews = SyncStudyReviewsUseCase(studyRepository),
                getCurrentStreak = GetCurrentStudyStreakUseCase(studyRepository),
                hasStudiedToday = HasStudiedTodayUseCase(studyRepository)
            )
        }
    }
