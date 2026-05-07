package com.example.flashcardapp.domain.usecase.notification

import com.example.flashcardapp.data.repository.NotificationRepository

class GetUnreadNotificationCountUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return notificationRepository.getUnreadCount()
    }
}
