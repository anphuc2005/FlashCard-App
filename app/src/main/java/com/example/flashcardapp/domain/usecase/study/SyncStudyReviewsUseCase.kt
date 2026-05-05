package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class SyncStudyReviewsUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<Int> {
//        return studyRepository.syncReviews()
        return Result.success(0)
    }
}
