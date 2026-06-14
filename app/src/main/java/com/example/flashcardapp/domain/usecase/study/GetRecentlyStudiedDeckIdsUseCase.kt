package com.example.flashcardapp.domain.usecase.study

import com.example.flashcardapp.data.repository.StudyRepository

class GetRecentlyStudiedDeckIdsUseCase(
    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return studyRepository.getRecentlyStudiedDeckIds()
    }
}
