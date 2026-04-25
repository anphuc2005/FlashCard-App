package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.api.ProfileApiService
import com.example.flashcardapp.data.datasource.remote.model.profile.UpdateProfileRequest
import com.example.flashcardapp.data.datasource.remote.model.profile.toDomain
import com.example.flashcardapp.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val profileApiService: ProfileApiService
) {

    suspend fun getMyProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = profileApiService.getMyProfile()
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateMyProfile(displayName: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = profileApiService.updateMyProfile(
                    UpdateProfileRequest(displayName = displayName.trim())
                )
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
