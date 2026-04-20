package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<ForgotPasswordResponse>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<ResetPasswordResponse>>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<ApiResponse<GoogleLoginResponse>>

}

