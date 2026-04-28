package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class HasStudiedTodayUseCase(
    private val studyRepository: StudyRepository
) {
    operator fun invoke(): Boolean {
        return studyRepository.hasStudiedToday()
    }
}
