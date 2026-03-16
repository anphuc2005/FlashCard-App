package com.example.flashcardapp.ui.feature.auth.logic.validation

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

