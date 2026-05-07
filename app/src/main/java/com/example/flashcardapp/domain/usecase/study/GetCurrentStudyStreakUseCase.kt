package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class GetCurrentStudyStreakUseCase(
    private val studyRepository: StudyRepository
) {
    operator fun invoke(): Int {
        return studyRepository.getCurrentStreak()
    }
}
