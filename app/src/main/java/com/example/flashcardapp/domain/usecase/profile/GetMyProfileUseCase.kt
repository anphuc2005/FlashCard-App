package com.example.flashcardapp.domain.usecase.profile

import com.example.flashcardapp.data.repository.ProfileRepository
import com.example.flashcardapp.domain.model.UserProfile

class GetMyProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<UserProfile> {
        return profileRepository.getMyProfile(forceRefresh = forceRefresh)
    }

    fun getCachedProfile(): UserProfile? = profileRepository.getCachedProfile()
}
