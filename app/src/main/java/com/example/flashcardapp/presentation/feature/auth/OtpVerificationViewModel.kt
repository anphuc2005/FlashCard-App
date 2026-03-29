package com.example.flashcardapp.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.usecase.auth.VerifyOtpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthOperationState>(AuthOperationState.Idle)
    val uiState: StateFlow<AuthOperationState> = _uiState.asStateFlow()

    private var email: String = ""

    fun setContext(email: String?) {
        if (!email.isNullOrBlank()) this.email = email
    }

    fun verify(otp: String) {
        if (otp.length < 6) {
            _uiState.value = AuthOperationState.Error("OTP không hợp lệ")
            return
        }
        _uiState.value = AuthOperationState.Loading
        viewModelScope.launch {
            val result = verifyOtpUseCase(email, otp)
            result.onSuccess { response ->
                _uiState.value = AuthOperationState.Success(
                    message = response.message,
                    email = email
                )
            }
            result.onFailure {
                _uiState.value = AuthOperationState.Error(
                    it.message ?: "Đã xảy ra lỗi"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthOperationState.Idle
    }
}


