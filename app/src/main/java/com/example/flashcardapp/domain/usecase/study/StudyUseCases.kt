package com.example.flashcardapp.domain.usecase.study

data class StudyUseCases(
    val getSessionCards: GetStudySessionCardsUseCase,
    val getReviewedCardIds: GetReviewedCardIdsUseCase,
    val saveReview: SaveStudyReviewUseCase,
    val syncReviews: SyncStudyReviewsUseCase
)
