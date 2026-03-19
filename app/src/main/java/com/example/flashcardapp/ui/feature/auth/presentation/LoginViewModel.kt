package com.example.flashcardapp.ui.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.ui.feature.auth.domain.LoginUseCase
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.state.AuthOperationState
import com.example.flashcardapp.ui.feature.auth.state.LoginFormState
import com.example.flashcardapp.ui.feature.auth.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState<LoginResponse>>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState<LoginResponse>> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _formState.value = _formState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun submit() {
        val current = _formState.value
        val emailResult = AuthValidator.validateEmail(current.email.trim())
        val passwordResult = AuthValidator.validatePassword(current.password)

        val hasError = !emailResult.isValid || !passwordResult.isValid
        _formState.value = current.copy(
            emailError = emailResult.errorMessage,
            passwordError = passwordResult.errorMessage
        )
        if (hasError) return

        val request = LoginRequest(
            email = current.email.trim(),
            password = current.password
        )

        viewModelScope.launch {
            _uiState.value = AuthOperationState.Loading
            val result = loginUseCase(request)
            _uiState.value = result.fold(
                onSuccess = { AuthOperationState.Success(it) },
                onFailure = { AuthOperationState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }
}
