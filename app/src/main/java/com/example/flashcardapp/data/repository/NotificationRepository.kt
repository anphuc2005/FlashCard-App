package com.example.flashcardapp.data.repository

import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.data.datasource.remote.api.NotificationApiService
import com.example.flashcardapp.domain.model.UserNotificationPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val notificationApiService: NotificationApiService
) {

    suspend fun getUnreadCount(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = notificationApiService.getUnreadCount()
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.count.coerceAtLeast(0))
                } else {
                    Result.failure(
                        IllegalStateException(
                            response.message ?: "Không thể tải số thông báo chưa đọc"
                        )
                    )
                }
            } catch (throwable: Throwable) {
                Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
            }
        }
    }

    suspend fun getNotifications(page: Int = 0, size: Int = 20): Result<UserNotificationPage> {
        return withContext(Dispatchers.IO) {
            try {
                val response = notificationApiService.getNotifications(
                    page = page.coerceAtLeast(0),
                    size = size.coerceIn(1, 50)
                )
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.toDomain())
                } else {
                    Result.failure(
                        IllegalStateException(
                            response.message ?: "Không thể tải danh sách thông báo"
                        )
                    )
                }
            } catch (throwable: Throwable) {
                Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
            }
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = notificationApiService.markAsRead(notificationId)
                if (response.isSuccess()) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        IllegalStateException(
                            response.message ?: "Không thể đánh dấu thông báo đã đọc"
                        )
                    )
                }
            } catch (throwable: Throwable) {
                Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
            }
        }
    }
}
