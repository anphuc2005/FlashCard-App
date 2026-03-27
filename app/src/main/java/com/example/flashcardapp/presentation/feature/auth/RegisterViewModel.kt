package com.example.flashcardapp.presentation.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel : ViewModel() {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState> = _uiState.asStateFlow()

    private var fullName = ""
    private var email = ""
    private var password = ""
    private var confirmPassword = ""

    fun onFullNameChanged(newFullName: String) {
        fullName = newFullName
        validateForm()
    }

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        validateForm()
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
        validateForm()
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        validateForm()
    }

    fun submit() {
        if (validateForm()) {
            _uiState.value = AuthOperationState.Loading
            // TODO: Implement register logic
            _uiState.value = AuthOperationState.Success("Registration successful")
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }

    private fun validateForm(): Boolean {
        val errors = RegisterFormState()
        var isValid = true

        if (fullName.isBlank()) {
            _formState.value = errors.copy(fullNameError = "Full name is required")
            isValid = false
        }

        if (email.isBlank()) {
            _formState.value = errors.copy(emailError = "Email is required")
            isValid = false
        }

        if (password.isBlank()) {
            _formState.value = errors.copy(passwordError = "Password is required")
            isValid = false
        }

        if (confirmPassword != password) {
            _formState.value = errors.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }

        if (isValid) {
            _formState.value = RegisterFormState()
        }

        return isValid
    }
}

