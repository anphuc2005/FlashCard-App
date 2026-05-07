package com.example.flashcardapp.data.datasource.remote.api

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("url")
    val url: String // Điều chỉnh theo cấu trúc JSON của API
)

