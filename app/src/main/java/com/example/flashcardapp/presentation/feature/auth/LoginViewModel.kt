package com.example.flashcardapp.presentation.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState> = _uiState.asStateFlow()

    private var email = ""
    private var password = ""

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        validateForm()
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
        validateForm()
    }

    fun submit() {
        if (validateForm()) {
            _uiState.value = AuthOperationState.Loading
            viewModelScope.launch {
                Log.d("LoginViewModel", "Attempting login with email: $email")
                val result = loginUseCase(email, password)
                result.onSuccess {
                    Log.d("LoginViewModel", "Login successful. Token: ${it.accessToken.take(20)}...")
                    _uiState.value = AuthOperationState.Success("Login successful")
                }
                result.onFailure { throwable ->
                    Log.e("LoginViewModel", "Login failed: ${throwable.message}", throwable)
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
        val errors = LoginFormState()
        var isValid = true

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

        if (isValid) {
            _formState.value = LoginFormState()
        }

        return isValid
    }
}

