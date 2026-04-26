package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.profile.UpdateProfileRequest
import com.example.flashcardapp.data.datasource.remote.model.profile.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApiService {

    @GET("users/me")
    suspend fun getMyProfile(): ApiResponse<UserProfileDto>

    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequest
    ): ApiResponse<UserProfileDto>
}
