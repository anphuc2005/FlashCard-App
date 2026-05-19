package com.example.flashcardapp.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flashcardapp.domain.model.UserProfile

@Entity(tableName = "profile_table")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = PROFILE_ID,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String? = null
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = createdAt
        )
    }

    companion object {
        const val PROFILE_ID = "current_user"
    }
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        createdAt = createdAt
    )
}
