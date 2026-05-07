package com.example.flashcardapp.presentation.feature.auth.login

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.GoogleLoginUseCase
import com.example.flashcardapp.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
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
                    _uiState.value = AuthOperationState.Success("Đăng nhập thành công")
                }
                result.onFailure { throwable ->
                    Log.e("LoginViewModel", "Login failed: ${throwable.message}", throwable)
                    _uiState.value = AuthOperationState.Error(
                        throwable.message ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun googleLogin(idToken: String) {
        _uiState.value = AuthOperationState.Loading
        viewModelScope.launch {
            Log.d("LoginViewModel", "Attempting Google Login")
            val result = googleLoginUseCase(idToken)
            result.onSuccess {
                Log.d("LoginViewModel", "Google Login successful")
                _uiState.value = AuthOperationState.Success("Đăng nhập Google thành công")
            }
            result.onFailure { throwable ->
                Log.e("LoginViewModel", "Google Login failed: ${throwable.message}", throwable)
                _uiState.value = AuthOperationState.Error(
                    throwable.message ?: "Đăng nhập Google thất bại"
                )
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

        if (isValid) {
            _formState.value = LoginFormState()
        }

        return isValid
    }
}

