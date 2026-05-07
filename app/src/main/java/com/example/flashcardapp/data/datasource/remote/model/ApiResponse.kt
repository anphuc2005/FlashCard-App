package com.example.flashcardapp.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    val timestamp: String? = null
) {
    fun isSuccess(): Boolean = success == true || (status != null && status in 200..299)
}
