package com.example.flashcardapp.domain.model

data class UserNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String?
)

data class UserNotificationPage(
    val content: List<UserNotification> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
    val isLast: Boolean = true,
    val isFirst: Boolean = true
)
