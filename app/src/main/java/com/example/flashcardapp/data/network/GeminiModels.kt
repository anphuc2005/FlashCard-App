package com.example.flashcardapp.data.network

import com.google.gson.annotations.SerializedName

// Request models

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>
)

data class GeminiContent(
    @SerializedName("parts")
    val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text")
    val text: String
)

// Response models

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContent? = null
)

