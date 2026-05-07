package com.example.flashcardapp.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class SubmitReportRequestDto(
    @SerializedName("targetDeckId") val targetDeckId: String,
    @SerializedName("reason") val reason: String
)
