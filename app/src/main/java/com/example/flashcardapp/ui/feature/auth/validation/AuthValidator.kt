package com.example.flashcardapp.ui.feature.auth.validation

import android.util.Patterns

object AuthValidator {

    private const val MIN_PASSWORD_LENGTH = 6

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult(false, "$fieldName không được để trống")
        } else {
            ValidationResult(true)
        }
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(false, "Email không được để trống")
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Email không đúng định dạng")
        }
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult(false, "Mật khẩu không được để trống")
        return if (password.length >= MIN_PASSWORD_LENGTH) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Mật khẩu phải có ít nhất $MIN_PASSWORD_LENGTH ký tự")
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(false, "Vui lòng nhập lại mật khẩu")
        }
        return if (password == confirmPassword) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Mật khẩu nhập lại không khớp")
        }
    }
}
