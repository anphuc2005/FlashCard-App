package com.example.flashcardapp.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

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
            viewModelScope.launch {
                val result = forgotPasswordUseCase(email)
                result.onSuccess {
                    _uiState.value = AuthOperationState.Success(
                        message = "Verification code sent to your email",
                        email = email
                    )
                }
                result.onFailure { throwable ->
                    _uiState.value = AuthOperationState.Error(
                        throwable.message ?: "An unexpected error occurred"
                    )
                }
            }
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
        } else if (!email.contains("@")) {
            _formState.value = errors.copy(emailError = "Invalid email format")
            isValid = false
        }

        if (isValid) {
            _formState.value = ForgotPasswordFormState()
        }

        return isValid
    }
}

