package com.example.flashcardapp.presentation.feature.auth.resetpassword

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.ResetPasswordUseCase
import com.example.flashcardapp.presentation.feature.auth.PasswordStrength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(ResetPasswordFormState())
    val formState: StateFlow<ResetPasswordFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState> = _uiState.asStateFlow()

    private var email: String = ""
    private var otp: String = ""
    private var newPass: String = ""
    private var confirmPass: String = ""

    fun setContext(email: String?, otp: String?) {
        if (!email.isNullOrBlank()) this.email = email
        if (!otp.isNullOrBlank()) this.otp = otp
    }

    fun onNewPasswordChanged(value: String) {
        newPass = value
        validate()
    }

    fun onConfirmPasswordChanged(value: String) {
        confirmPass = value
        validate()
    }

    fun submit() {
        if (!validate()) return
        _uiState.value = AuthOperationState.Loading
        viewModelScope.launch {
            val result = resetPasswordUseCase(email, otp, newPass)
            result.onSuccess { response ->
                _uiState.value = AuthOperationState.Success(response.message, email = email)
            }
            result.onFailure {
                _uiState.value = AuthOperationState.Error(it.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthOperationState.Idle
    }

    private fun validate(): Boolean {
        var isValid = true
        var passwordError: String? = null
        var confirmError: String? = null

        if (newPass.length < 6) {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        }
        if (confirmPass.isBlank()) {
            confirmError = "Vui lòng nhập lại mật khẩu"
            isValid = false
        } else if (newPass != confirmPass) {
            confirmError = "Mật khẩu không khớp"
            isValid = false
        }

        _formState.value = ResetPasswordFormState(
            passwordError = passwordError,
            confirmPasswordError = confirmError,
            strength = evaluateStrength(newPass)
        )
        return isValid
    }

    private fun evaluateStrength(password: String): PasswordStrength {
        if (password.isBlank()) return PasswordStrength.EMPTY

        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 5 -> PasswordStrength.STRONG
            score >= 4 -> PasswordStrength.GOOD
            score >= 3 -> PasswordStrength.FAIR
            else -> PasswordStrength.WEAK
        }
    }
}
