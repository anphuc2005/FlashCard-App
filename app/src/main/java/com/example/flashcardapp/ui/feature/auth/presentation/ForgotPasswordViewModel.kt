package com.example.flashcardapp.ui.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.ui.feature.auth.domain.ForgotPasswordUseCase
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.state.AuthOperationState
import com.example.flashcardapp.ui.feature.auth.state.ForgotPasswordFormState
import com.example.flashcardapp.ui.feature.auth.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(ForgotPasswordFormState())
    val formState: StateFlow<ForgotPasswordFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState<ForgotPasswordResponse>>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState<ForgotPasswordResponse>> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _formState.value = _formState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun submit() {
        val current = _formState.value
        val emailResult = AuthValidator.validateEmail(current.email.trim())
        _formState.value = current.copy(emailError = emailResult.errorMessage)
        if (!emailResult.isValid) return

        val request = ForgotPasswordRequest(email = current.email.trim())

        viewModelScope.launch {
            _uiState.value = AuthOperationState.Loading
            val result = forgotPasswordUseCase(request)
            _uiState.value = result.fold(
                onSuccess = { AuthOperationState.Success(it) },
                onFailure = { AuthOperationState.Error(it.message ?: "Forgot password failed") }
            )
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }
}
