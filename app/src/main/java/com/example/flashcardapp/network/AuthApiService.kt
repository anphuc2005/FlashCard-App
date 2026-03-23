package com.example.flashcardapp.network

import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // Update these endpoint paths to match your backend auth module.
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
}
