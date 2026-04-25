package com.example.flashcardapp.data.datasource.remote.model.profile

import com.example.flashcardapp.domain.model.UserProfile
import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class UpdateProfileRequest(
    @SerializedName("displayName") val displayName: String
)

fun UserProfileDto.toDomain(): UserProfile {
    return UserProfile(
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        createdAt = createdAt
    )
}
