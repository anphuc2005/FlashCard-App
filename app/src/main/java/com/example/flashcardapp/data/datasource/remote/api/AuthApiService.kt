package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse

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

