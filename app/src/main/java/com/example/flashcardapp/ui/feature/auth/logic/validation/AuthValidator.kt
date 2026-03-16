package com.example.flashcardapp.ui.feature.auth.logic.validation

import android.util.Patterns

object AuthValidator {

    private const val MIN_PASSWORD_LENGTH = 6

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult(false, "$fieldName is required")
        } else {
            ValidationResult(true)
        }
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(false, "Email is required")
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Invalid email format")
        }
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult(false, "Password is required")
        return if (password.length >= MIN_PASSWORD_LENGTH) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Password must be at least $MIN_PASSWORD_LENGTH characters")
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(false, "Confirm password is required")
        }
        return if (password == confirmPassword) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Confirm password does not match")
        }
    }
}

