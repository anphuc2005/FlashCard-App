package com.example.flashcardapp.ui.feature.auth.state

sealed interface AuthOperationState<out T> {
    data object Idle : AuthOperationState<Nothing>
    data object Loading : AuthOperationState<Nothing>
    data class Success<T>(val data: T) : AuthOperationState<T>
    data class Error(val message: String) : AuthOperationState<Nothing>
}
