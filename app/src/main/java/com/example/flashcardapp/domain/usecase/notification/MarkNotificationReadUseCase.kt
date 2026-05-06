package com.example.flashcardapp.domain.usecase.notification

import com.example.flashcardapp.data.repository.NotificationRepository

class MarkNotificationReadUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return notificationRepository.markAsRead(notificationId)
    }
}
