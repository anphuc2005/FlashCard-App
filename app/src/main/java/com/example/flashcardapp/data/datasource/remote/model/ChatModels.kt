package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.model.ChatSession
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.util.UUID

data class ChatSessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain(): ChatSession {
        return ChatSession(
            id = id,
            title = title?.takeIf { it.isNotBlank() } ?: "Cuộc trò chuyện mới",
            updatedAt = updatedAt.parseIsoTimestamp()
        )
    }
}

data class ChatMessageDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("sessionId") val sessionId: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    fun toDomain(fallbackSessionId: String? = null): ChatMessage {
        val safeSessionId = sessionId ?: fallbackSessionId
        val safeTimestamp = createdAt.parseIsoTimestamp()
        val safeId = id
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: generateFallbackMessageId(
                sessionId = safeSessionId,
                createdAt = createdAt,
                role = role,
                content = content
            )

        return ChatMessage(
            id = safeId,
            sessionId = safeSessionId,
            message = content.orEmpty(),
            sender = if (role.equals("assistant", ignoreCase = true)) "bot" else "user",
            timestamp = safeTimestamp
        )
    }
}

data class SendChatMessageRequest(
    @SerializedName("content") val content: String
)

private fun String?.parseIsoTimestamp(): Long {
    if (this.isNullOrBlank()) return System.currentTimeMillis()
    return runCatching { Instant.parse(this).toEpochMilli() }
        .getOrElse { System.currentTimeMillis() }
}

private fun generateFallbackMessageId(
    sessionId: String?,
    createdAt: String?,
    role: String?,
    content: String?
): String {
    val seed = listOf(
        sessionId.orEmpty(),
        createdAt.orEmpty(),
        role.orEmpty(),
        content.orEmpty()
    ).joinToString("|")

    if (seed.isBlank()) {
        return UUID.randomUUID().toString()
    }
    return UUID.nameUUIDFromBytes(seed.toByteArray(Charsets.UTF_8)).toString()
}
