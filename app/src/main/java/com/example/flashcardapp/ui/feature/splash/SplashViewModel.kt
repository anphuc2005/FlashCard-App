package com.example.flashcardapp.ui.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.AppSessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// SplashViewModel quyết định state điều hướng từ dữ liệu session local.
class SplashViewModel(
    private val sessionManager: AppSessionManager
) : ViewModel() {

    sealed class SplashState {
        data class Loading(val progress: Int) : SplashState()
        object NavigateToOnBoarding : SplashState()
        object NavigateToLogin : SplashState()
        object NavigateToHome : SplashState()
    }

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading(0))
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        startLoading()
    }

    private fun startLoading() {
        viewModelScope.launch {
            // Giữ splash ngắn gọn trước khi quyết định hướng đi.
            for (value in 0..100 step STEP_SIZE) {
                _splashState.value = SplashState.Loading(value.coerceAtMost(100))
                delay(FRAME_DELAY_MS)
            }

            // Điều hướng dựa trên onboarding và session đã lưu trên máy.
            delay(COMPLETION_DELAY_MS)
            val nextState = when {
                !sessionManager.hasOnboarded -> SplashState.NavigateToOnBoarding
                sessionManager.isLoggedIn -> SplashState.NavigateToHome
                else -> SplashState.NavigateToLogin
            }
            _splashState.value = nextState
        }
    }

    private companion object {
        const val STEP_SIZE = 4
        const val FRAME_DELAY_MS = 45L
        const val COMPLETION_DELAY_MS = 200L
    }
}
