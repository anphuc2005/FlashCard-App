package com.example.flashcardapp.presentation.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel : ViewModel() {

    private val _formState = MutableStateFlow(ForgotPasswordFormState())
    val formState: StateFlow<ForgotPasswordFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState> = _uiState.asStateFlow()

    private var email = ""

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        validateForm()
    }

    fun submit() {
        if (validateForm()) {
            _uiState.value = AuthOperationState.Loading
            // TODO: Implement forgot password logic
            _uiState.value = AuthOperationState.Success("Verification code sent")
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }

    private fun validateForm(): Boolean {
        val errors = ForgotPasswordFormState()
        var isValid = true

        if (email.isBlank()) {
            _formState.value = errors.copy(emailError = "Email is required")
            isValid = false
        }

        if (isValid) {
            _formState.value = ForgotPasswordFormState()
        }

        return isValid
    }
}

