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
                    _uiState.value = AuthOperationState.Success("Đăng ký thành công")
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
        val errors = RegisterFormState()
        var isValid = true

        if (fullName.isBlank()) {
            _formState.value = errors.copy(fullNameError = "Vui lòng nhập họ và tên")
            isValid = false
        }

        if (email.isBlank()) {
            _formState.value = errors.copy(emailError = "Vui lòng nhập email")
            isValid = false
        } else if (!email.contains("@")) {
            _formState.value = errors.copy(emailError = "Email không đúng định dạng")
            isValid = false
        }

        if (password.isBlank()) {
            _formState.value = errors.copy(passwordError = "Vui lòng nhập mật khẩu")
            isValid = false
        } else if (password.length < 6) {
            _formState.value = errors.copy(passwordError = "Mật khẩu phải có ít nhất 6 ký tự")
            isValid = false
        }

        if (confirmPassword != password) {
            _formState.value = errors.copy(confirmPasswordError = "Mật khẩu xác nhận không khớp")
            isValid = false
        }

        if (isValid) {
            _formState.value = RegisterFormState()
        }

        return isValid
    }
}

