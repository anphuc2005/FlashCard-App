package com.example.flashcardapp.presentation.feature.auth.forgotpassword

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

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
                        message = "Mã xác minh đã được gửi tới email của bạn",
                        email = email
                    )
                }
                result.onFailure { throwable ->
                    _uiState.value = AuthOperationState.Error(
                        throwable.message ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
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
            _formState.value = errors.copy(emailError = "Vui lòng nhập email")
            isValid = false
        } else if (!email.contains("@")) {
            _formState.value = errors.copy(emailError = "Email không đúng định dạng")
            isValid = false
        }

        if (isValid) {
            _formState.value = ForgotPasswordFormState()
        }

        return isValid
    }
}

