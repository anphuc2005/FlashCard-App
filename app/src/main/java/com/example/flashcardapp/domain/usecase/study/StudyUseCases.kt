package com.example.flashcardapp.domain.usecase.study

data class StudyUseCases(
    val getSessionCards: GetStudySessionCardsUseCase,
    val getRecentSession: GetRecentStudySessionUseCase,
    val getSessionByDeck: GetStudySessionByDeckUseCase,
    val saveSessionState: SaveStudySessionStateUseCase,
    val deleteSessionByDeck: DeleteStudySessionByDeckUseCase,
    val getReviewedCardIds: GetReviewedCardIdsUseCase,
    val saveReview: SaveStudyReviewUseCase,
    val syncReviews: SyncStudyReviewsUseCase,
    val getCurrentStreak: GetCurrentStudyStreakUseCase,
    val hasStudiedToday: HasStudiedTodayUseCase
)
