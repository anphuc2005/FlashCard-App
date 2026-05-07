package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.UserNotification
import com.example.flashcardapp.domain.model.UserNotificationPage
import com.google.gson.annotations.SerializedName

data class UnreadCountDto(
    @SerializedName("count") val count: Int = 0
)

data class UserNotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("isRead") val isRead: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    fun toDomain(): UserNotification {
        return UserNotification(
            id = id,
            title = title.orEmpty(),
            message = message.orEmpty(),
            type = type ?: "SYSTEM",
            isRead = isRead,
            createdAt = createdAt
        )
    }
}

data class UserNotificationPageDto(
    @SerializedName("content") val content: List<UserNotificationDto> = emptyList(),
    @SerializedName("currentPage") val currentPage: Int = 0,
    @SerializedName(value = "pageSize", alternate = ["size"]) val pageSize: Int = 20,
    @SerializedName("totalElements") val totalElements: Long = 0L,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName(value = "isLast", alternate = ["last"]) val isLast: Boolean = true,
    @SerializedName(value = "isFirst", alternate = ["first"]) val isFirst: Boolean = true
) {
    fun toDomain(): UserNotificationPage {
        return UserNotificationPage(
            content = content.map { it.toDomain() },
            currentPage = currentPage,
            pageSize = pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            isLast = isLast,
            isFirst = isFirst
        )
    }
}
