package com.example.flashcardapp.presentation.feature.auth.register

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

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
            viewModelScope.launch {
                val result = registerUseCase(email, password, fullName)
                result.onSuccess {
                    _uiState.value = AuthOperationState.Success("Registration successful")
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
        val errors = RegisterFormState()
        var isValid = true

        if (fullName.isBlank()) {
            _formState.value = errors.copy(fullNameError = "Full name is required")
            isValid = false
        }

        if (email.isBlank()) {
            _formState.value = errors.copy(emailError = "Email is required")
            isValid = false
        } else if (!email.contains("@")) {
            _formState.value = errors.copy(emailError = "Invalid email format")
            isValid = false
        }

        if (password.isBlank()) {
            _formState.value = errors.copy(passwordError = "Password is required")
            isValid = false
        } else if (password.length < 6) {
            _formState.value = errors.copy(passwordError = "Password must be at least 6 characters")
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

