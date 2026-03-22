package com.example.flashcardapp.ui.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.ui.feature.auth.domain.RegisterUseCase
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse
import com.example.flashcardapp.ui.feature.auth.state.AuthOperationState
import com.example.flashcardapp.ui.feature.auth.state.RegisterFormState
import com.example.flashcardapp.ui.feature.auth.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState<RegisterResponse>>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState<RegisterResponse>> = _uiState.asStateFlow()

    fun onFullNameChanged(fullName: String) {
        _formState.value = _formState.value.copy(
            fullName = fullName,
            fullNameError = null
        )
    }

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

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _formState.value = _formState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun submit() {
        if (_uiState.value is AuthOperationState.Loading) return

        val current = _formState.value

        val fullNameResult = AuthValidator.validateRequired(current.fullName.trim(), "Họ và tên")
        val emailResult = AuthValidator.validateEmail(current.email.trim())
        val passwordResult = AuthValidator.validatePassword(current.password)
        val confirmPasswordResult = AuthValidator.validateConfirmPassword(
            password = current.password,
            confirmPassword = current.confirmPassword
        )

        val hasError = !fullNameResult.isValid ||
            !emailResult.isValid ||
            !passwordResult.isValid ||
            !confirmPasswordResult.isValid

        _formState.value = current.copy(
            fullNameError = fullNameResult.errorMessage,
            emailError = emailResult.errorMessage,
            passwordError = passwordResult.errorMessage,
            confirmPasswordError = confirmPasswordResult.errorMessage
        )
        if (hasError) return

        val request = RegisterRequest(
            fullName = current.fullName.trim(),
            email = current.email.trim(),
            password = current.password,
            confirmPassword = current.confirmPassword
        )

        viewModelScope.launch {
            _uiState.value = AuthOperationState.Loading
            val result = registerUseCase(request)
            _uiState.value = result.fold(
                onSuccess = { AuthOperationState.Success(it) },
                onFailure = { AuthOperationState.Error(it.message ?: "Đăng ký thất bại") }
            )
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }
}
