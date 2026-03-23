package com.example.flashcardapp.ui.feature.auth.validation

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
