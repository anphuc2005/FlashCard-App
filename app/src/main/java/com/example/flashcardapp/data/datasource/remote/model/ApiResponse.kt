package com.example.flashcardapp.data.datasource.remote.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

