package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository
import com.example.flashcardapp.domain.model.study.StudyRecentSession

class GetRecentStudySessionUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<StudyRecentSession?> {
        return studyRepository.getRecentSession()
    }
}
