package com.example.flashcardapp.domain.usecase.profile

import com.example.flashcardapp.data.repository.ProfileRepository
import com.example.flashcardapp.domain.model.UserProfile

class UpdateMyProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        displayName: String,
        imageUrl: String? = null
    ): Result<UserProfile> {
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Họ và tên không được để trống"))
        }
        return profileRepository.updateMyProfile(
            displayName = displayName,
            imageUrl = imageUrl
        )
    }
}
