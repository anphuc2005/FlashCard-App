package com.example.flashcardapp.presentation.feature.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.FlashcardApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class SplashViewModel(application: Application) : AndroidViewModel(application) {

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
            // Show progress for 1 second
            for (value in 0..100 step STEP_SIZE) {
                _splashState.value = SplashState.Loading(value.coerceAtMost(100))
                delay(FRAME_DELAY_MS)
            }

            // After loading completes, check user status
            delay(COMPLETION_DELAY_MS)
            val sessionManager = (getApplication<Application>() as FlashcardApp).container.sessionManager

            val nextState = runCatching {
                when {
                    !sessionManager.hasOnboarded -> SplashState.NavigateToOnBoarding
                    !sessionManager.isLoggedIn -> SplashState.NavigateToLogin
                    sessionManager.accessToken.isNullOrBlank() -> {
                        sessionManager.clearLoginSession()
                        SplashState.NavigateToLogin
                    }
                    sessionManager.isAuthExpired -> SplashState.NavigateToLogin
                    isSessionUsableForStartup() -> SplashState.NavigateToHome
                    hasOfflineDeckData() -> SplashState.NavigateToHome
                    else -> {
                        sessionManager.clearLoginSession()
                        SplashState.NavigateToLogin
                    }
                }
            }.getOrElse {
                if (!sessionManager.isAuthExpired && hasOfflineDeckData()) {
                    SplashState.NavigateToHome
                } else {
                    sessionManager.clearLoginSession()
                    SplashState.NavigateToLogin
                }
            }
            _splashState.value = nextState
        }
    }

    private suspend fun isSessionUsableForStartup(): Boolean {
        val appContainer = (getApplication<Application>() as FlashcardApp).container
        val sessionManager = appContainer.sessionManager
        val result = runCatching {
            withContext(Dispatchers.IO) {
                appContainer.deckRepository.getAllDecksFromApi()
            }
        }.getOrElse { throwable ->
            return when (throwable) {
                is IOException -> true
                else -> false
            }
        }
        if (result.isSuccess) return true

        val throwable = result.exceptionOrNull()
        return when (throwable) {
            is HttpException -> {
                val unauthorized = throwable.code() == HTTP_UNAUTHORIZED || throwable.code() == HTTP_FORBIDDEN
                if (unauthorized) {
                    sessionManager.markAuthExpired()
                }
                return false
            }
            is IOException -> true
            else -> true
        }
    }

    private suspend fun hasOfflineDeckData(): Boolean {
        val appContainer = (getApplication<Application>() as FlashcardApp).container
        return runCatching {
            withContext(Dispatchers.IO) {
                appContainer.deckRepository.hasOfflineDeckData()
            }
        }.getOrDefault(false)
    }

    private companion object {
        const val STEP_SIZE = 4
        const val FRAME_DELAY_MS = 45L
        const val COMPLETION_DELAY_MS = 200L
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
    }
}

