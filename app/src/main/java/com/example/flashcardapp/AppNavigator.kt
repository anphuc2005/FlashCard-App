package com.example.flashcardapp

interface AppNavigator {
    fun openLogin(clearBackStack: Boolean)
    fun openRegister()
    fun openForgotPassword()
    fun openOtpVerification()
    fun completeOnboarding()
    fun completeLogin(accessToken: String?)
    fun navigateBack()
}
