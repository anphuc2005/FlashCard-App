package com.example.flashcardapp.data.datasource.remote.model

data class ApiResponse<T>(
    val status: Int,
    val message: String?,
    val data: T?,
    val timestamp: String? = null
) {
    fun isSuccess(): Boolean = status == 200
}
