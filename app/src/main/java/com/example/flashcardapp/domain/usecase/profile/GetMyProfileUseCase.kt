package com.example.flashcardapp.domain.usecase.profile

import com.example.flashcardapp.data.repository.ProfileRepository
import com.example.flashcardapp.domain.model.UserProfile

class GetMyProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return profileRepository.getMyProfile()
    }
}
