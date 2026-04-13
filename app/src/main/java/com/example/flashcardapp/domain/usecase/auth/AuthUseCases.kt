package com.example.flashcardapp.domain.usecase.auth

data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val forgotPassword: ForgotPasswordUseCase,
    val verifyOtp: VerifyOtpUseCase,
    val resetPassword: ResetPasswordUseCase
)

