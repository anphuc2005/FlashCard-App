package com.example.flashcardapp.ui.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    init {
        startLoading()
    }

    private fun startLoading() {
        viewModelScope.launch {
            for (value in 0..100 step STEP_SIZE) {
                _progress.value = value.coerceAtMost(100)
                delay(FRAME_DELAY_MS)
            }
            _progress.value = 100
        }
    }

    private companion object {
        const val STEP_SIZE = 4
        const val FRAME_DELAY_MS = 45L
    }
}
