package com.example.flashcardapp.domain.model

data class UserProfile(
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String? = null
)
