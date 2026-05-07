package com.example.flashcardapp.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model")
    val model: String = "openai/gpt-oss-120b", // Default Groq model
    @SerializedName("messages")
    val messages: List<GroqMessage>,
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1024
)

data class GroqMessage(
    @SerializedName("role")
    val role: String, // "user" or "assistant"
    @SerializedName("content")
    val content: String
)

// Response models

data class GroqResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("object")
    val obj: String? = null,
    @SerializedName("created")
    val created: Long? = null,
    @SerializedName("model")
    val model: String? = null,
    @SerializedName("choices")
    val choices: List<GroqChoice>? = null,
    @SerializedName("usage")
    val usage: GroqUsage? = null
)

data class GroqChoice(
    @SerializedName("index")
    val index: Int? = null,
    @SerializedName("message")
    val message: GroqMessage? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class GroqUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)

