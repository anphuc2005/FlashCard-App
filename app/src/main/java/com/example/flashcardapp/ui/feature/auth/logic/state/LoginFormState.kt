package com.example.flashcardapp.ui.feature.auth.logic.state

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

