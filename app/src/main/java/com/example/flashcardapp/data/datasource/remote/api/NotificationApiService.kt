package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.UnreadCountDto
import com.example.flashcardapp.data.datasource.remote.model.UserNotificationPageDto
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<UnreadCountDto>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<UserNotificationPageDto>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): ApiResponse<Any?>
}
