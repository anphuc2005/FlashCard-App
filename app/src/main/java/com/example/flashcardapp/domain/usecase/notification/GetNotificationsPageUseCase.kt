package com.example.flashcardapp.domain.usecase.notification

import com.example.flashcardapp.data.repository.NotificationRepository
import com.example.flashcardapp.domain.model.UserNotificationPage

class GetNotificationsPageUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<UserNotificationPage> {
        return notificationRepository.getNotifications(page = page, size = size)
    }
}
